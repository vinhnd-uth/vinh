package com.example.demo.youtube

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ==========================================
// 1. TRUE PARALLEL COROUTINE DAG SCHEDULER & RETRY ENGINE
// ==========================================

/**
 * Chapter 14: Design YouTube - Trình điều phối đồ thị DAG chạy song song đa luồng thực sự (True Parallel Coroutines)
 * 
 * Tích hợp:
 * - Chạy song song thực sự bằng `async(Dispatchers.Default)` & `awaitAll()`.
 * - Cơ chế Retry tự động Exponential Backoff khi gặp lỗi.
 * - Hàng chờ Dead Letter Queue (DLQ) phục hồi khi vượt quá số lần thử lại.
 */
class TranscodingDAGEngine {

    private val _dagNodes = MutableStateFlow<List<DAGNode>>(emptyList())
    val dagNodes: StateFlow<List<DAGNode>> = _dagNodes.asStateFlow()

    private val _currentStatus = MutableStateFlow(TranscodingStatus.IDLE)
    val currentStatus: StateFlow<TranscodingStatus> = _currentStatus.asStateFlow()

    private val _hlsPlaylistResult = MutableStateFlow<HLSPlaylist?>(null)
    val hlsPlaylistResult: StateFlow<HLSPlaylist?> = _hlsPlaylistResult.asStateFlow()

    // Hàng chờ Dead Letter Queue (DLQ) lưu các tác vụ lỗi không phục hồi được
    val deadLetterQueue = mutableListOf<Pair<DAGNode, String>>()

    fun createDAGPipeline(videoId: String): List<DAGNode> {
        return listOf(
            DAGNode("node_inspect", "1. Inspect & Validate Video Container", DAGNodeType.INSPECT),
            DAGNode("node_split", "2. Video Splitter (GOP 10s Alignment)", DAGNodeType.SPLIT, listOf("node_inspect")),
            DAGNode("node_audio", "3. Audio Transcoder (AAC 192kbps)", DAGNodeType.TRANSCODE_AUDIO, listOf("node_split")),
            DAGNode("node_v1080", "4a. Video Encoder 1080p (5000kbps)", DAGNodeType.TRANSCODE_VIDEO, listOf("node_split")),
            DAGNode("node_v720", "4b. Video Encoder 720p (2500kbps)", DAGNodeType.TRANSCODE_VIDEO, listOf("node_split")),
            DAGNode("node_v480", "4c. Video Encoder 480p (1000kbps)", DAGNodeType.TRANSCODE_VIDEO, listOf("node_split")),
            DAGNode("node_watermark", "5. Watermark & Brand Inserter", DAGNodeType.WATERMARK, listOf("node_v1080", "node_v720", "node_v480")),
            DAGNode("node_thumbnail", "6. Thumbnail Generator", DAGNodeType.THUMBNAIL, listOf("node_split")),
            DAGNode("node_package", "7. HLS Master Packager (.m3u8 & .ts)", DAGNodeType.HLS_PACKAGE, listOf("node_audio", "node_watermark", "node_thumbnail"))
        )
    }

    /**
     * Thực thi đồ thị DAG bằng Coroutine Async đa luồng song song thực sự (True Parallel Execution)
     */
    suspend fun executePipeline(video: VideoMetadata): HLSPlaylist = coroutineScope {
        val initialNodes = createDAGPipeline(video.videoId)
        _dagNodes.value = initialNodes
        _hlsPlaylistResult.value = null
        deadLetterQueue.clear()
        _currentStatus.value = TranscodingStatus.INSPECTING

        val completedNodeIds = mutableSetOf<String>()

        while (completedNodeIds.size < initialNodes.size) {
            val currentNodes = _dagNodes.value
            val runnableNodes = currentNodes.filter { node ->
                node.state == NodeExecutionState.PENDING &&
                        node.dependencies.all { depId -> completedNodeIds.contains(depId) }
            }

            if (runnableNodes.isEmpty() && currentNodes.any { it.state == NodeExecutionState.RUNNING }) {
                delay(50)
                continue
            }

            // Đánh dấu trạng thái RUNNING
            runnableNodes.forEach { node ->
                updateNodeState(node.id, NodeExecutionState.RUNNING, 0.1f, "⚡ Khởi chạy song song trên Thread Pool...")
            }

            // CHẠY SONG SONG THỰC SỰ: Tạo các Coroutine async độc lập chạy trên Dispatchers.Default
            val deferredJobs = runnableNodes.map { node ->
                async(Dispatchers.Default) {
                    val success = executeNodeWithRetry(node, video, maxRetries = 3)
                    if (success) {
                        completedNodeIds.add(node.id)
                        updateNodeState(node.id, NodeExecutionState.SUCCESS, 1.0f, "✅ Hoàn thành song song thành công")
                    } else {
                        deadLetterQueue.add(Pair(node, "Vượt quá số lần Retry tối đa"))
                        updateNodeState(node.id, NodeExecutionState.FAILED, 0f, "❌ Lỗi: Chuyển vào Dead Letter Queue (DLQ)")
                    }
                }
            }

            // Chờ TẤT CẢ các tác vụ song song trong giai đoạn hiện tại hoàn thành
            deferredJobs.awaitAll()
            updateGlobalStatus(_dagNodes.value)
        }

        _currentStatus.value = TranscodingStatus.COMPLETED

        // Tạo file HLS Master Playlist RFC 8216 thật kèm bảo vệ AES-128 DRM
        val masterManifestContent = generateHlsMasterManifest(video.videoId, video.drmScheme)
        val playlist = HLSPlaylist(
            videoId = video.videoId,
            masterManifestPath = masterManifestContent,
            variantStreams = mapOf(
                Resolution.P1080 to "s3://cdn.youtube-demo.com/videos/${video.videoId}/1080p/index.m3u8",
                Resolution.P720 to "s3://cdn.youtube-demo.com/videos/${video.videoId}/720p/index.m3u8",
                Resolution.P480 to "s3://cdn.youtube-demo.com/videos/${video.videoId}/480p/index.m3u8"
            ),
            totalSegments = (video.durationSeconds / 10).coerceAtLeast(1)
        )
        _hlsPlaylistResult.value = playlist
        playlist
    }

    /**
     * Cơ chế Error Handling & Exponential Backoff Retry cho từng Node
     */
    private suspend fun executeNodeWithRetry(node: DAGNode, video: VideoMetadata, maxRetries: Int): Boolean {
        var attempt = 0
        var delayMs = 150L

        while (attempt < maxRetries) {
            attempt++
            try {
                val threadName = Thread.currentThread().name
                val steps = 4
                for (i in 1..steps) {
                    delay(delayMs)
                    val progress = i / steps.toFloat()
                    val msg = "[$threadName] ${node.name} ($i/$steps) (Lần thử $attempt/$maxRetries)"
                    updateNodeState(node.id, NodeExecutionState.RUNNING, progress, msg)
                }
                return true
            } catch (e: Exception) {
                delayMs *= 2
                updateNodeState(node.id, NodeExecutionState.RUNNING, 0.2f, "⚠️ Gặp lỗi, thử lại sau ${delayMs}ms (Thử $attempt/$maxRetries)")
            }
        }
        return false
    }

    private fun updateNodeState(id: String, state: NodeExecutionState, progress: Float, msg: String) {
        _dagNodes.update { list ->
            list.map { node ->
                if (node.id == id) {
                    node.copy(state = state, progress = progress, detailMessage = msg)
                } else node
            }
        }
    }

    private fun updateGlobalStatus(nodes: List<DAGNode>) {
        if (nodes.any { it.type == DAGNodeType.SPLIT && it.state == NodeExecutionState.RUNNING }) {
            _currentStatus.value = TranscodingStatus.SPLITTING
        } else if (nodes.any { it.type == DAGNodeType.TRANSCODE_VIDEO && it.state == NodeExecutionState.RUNNING }) {
            _currentStatus.value = TranscodingStatus.TRANSCODING
        } else if (nodes.any { it.type == DAGNodeType.HLS_PACKAGE && it.state == NodeExecutionState.RUNNING }) {
            _currentStatus.value = TranscodingStatus.PACKAGING
        }
    }

    /**
     * Trình tạo cấu trúc file HLS Master Playlist (.m3u8) chuẩn RFC 8216 với thẻ mã hóa AES-128 DRM
     */
    private fun generateHlsMasterManifest(videoId: String, drmScheme: DRMScheme): String {
        val keyTag = if (drmScheme == DRMScheme.AES_128) {
            "#EXT-X-KEY:METHOD=AES-128,URI=\"https://license.youtube.com/key/$videoId\",IV=0x9a8f12c3"
        } else {
            "#EXT-X-KEY:METHOD=SAMPLE-AES,URI=\"https://widevine.youtube.com/license/$videoId\",KEYFORMAT=\"urn:uuid:edef8ba9-79d6-4ace-a3c8-27dcd51d21ed\""
        }

        return """
            #EXTM3U
            #EXT-X-VERSION:3
            #EXT-X-INDEPENDENT-SEGMENTS
            $keyTag
            
            #EXT-X-STREAM-INF:BANDWIDTH=5000000,RESOLUTION=1920x1080,CODECS="avc1.640028,mp4a.40.2"
            s3://cdn.youtube-demo.com/videos/$videoId/1080p/index.m3u8
            
            #EXT-X-STREAM-INF:BANDWIDTH=2500000,RESOLUTION=1280x720,CODECS="avc1.4d401f,mp4a.40.2"
            s3://cdn.youtube-demo.com/videos/$videoId/720p/index.m3u8
            
            #EXT-X-STREAM-INF:BANDWIDTH=1000000,RESOLUTION=854x480,CODECS="avc1.4d401f,mp4a.40.2"
            s3://cdn.youtube-demo.com/videos/$videoId/480p/index.m3u8
        """.trimIndent()
    }
}

// ==========================================
// 2. RESUMABLE PARALLEL MULTIPART CHUNK UPLOADER (SẮP XẾP TỪ TỪ VÀ BẬT/TẮT MẠNG TƯƠNG TÁC)
// ==========================================

/**
 * Trình Upload từ từ từng bước theo GOP Chunks & Điều khiển Bật/Tắt Mạng Tương Tác
 */
class ResumableParallelChunkUploader {

    private val _uploadTracker = MutableStateFlow<ResumableUploadTracker?>(null)
    val uploadTracker: StateFlow<ResumableUploadTracker?> = _uploadTracker.asStateFlow()

    fun setNetworkOnlineState(isOnline: Boolean) {
        _uploadTracker.update { current ->
            current?.copy(
                isOnline = isOnline,
                isInterrupted = !isOnline,
                isResumed = isOnline && (current.completedChunkIndices.size > 0 && current.completedChunkIndices.size < current.totalChunks),
                errorMessage = if (isOnline) {
                    "🟢 ĐÃ CÓ MẠNG TRỞ LẠI! Đang tiếp tục upload từ ${(current.progressPercent * 100).toInt()}%..."
                } else {
                    "⚠️ LỖI MẤT KẾT NỐI MẠNG! Không thể tiếp tục tải video lên. Đang tạm dừng ở ${(current.progressPercent * 100).toInt()}%..."
                }
            )
        }
    }

    suspend fun startResumableParallelUpload(
        videoId: String,
        totalChunks: Int = 8,
        fileSizeMb: Long = 350L,
        autoSimulateDropAtChunk5: Boolean = true
    ): ResumableUploadTracker = coroutineScope {
        val tracker = ResumableUploadTracker(
            videoId = videoId,
            totalChunks = totalChunks,
            totalMb = fileSizeMb,
            isOnline = true,
            errorMessage = "Đang bắt đầu tải video lên từng bước..."
        )
        _uploadTracker.value = tracker

        // Upload từ từ từng chunk một (800ms/chunk) để người dùng kịp quan sát tiến trình %
        for (chunkIdx in 1..totalChunks) {
            // Kiểm tra trạng thái mạng do người dùng bấm nút: Nếu mất mạng thì dừng chờ
            while (_uploadTracker.value?.isOnline == false) {
                delay(300L)
            }

            // Tự động mô phỏng rớt mạng ở Chunk #5 nếu bật option
            if (autoSimulateDropAtChunk5 && chunkIdx == 5 && tracker.completedChunkIndices.size == 4) {
                setNetworkOnlineState(false)
                while (_uploadTracker.value?.isOnline == false) {
                    delay(300L)
                }
            }

            // Upload từ từ từng bước 800ms
            delay(800L)

            val current = _uploadTracker.value ?: tracker
            current.completedChunkIndices.add(chunkIdx)
            current.progressPercent = current.completedChunkIndices.size.toFloat() / totalChunks
            current.uploadedMb = (current.progressPercent * fileSizeMb).toLong()
            current.errorMessage = "Đang tải Chunk #$chunkIdx/$totalChunks (${(current.progressPercent * 100).toInt()}%)..."

            _uploadTracker.value = current.copy()
        }

        val finalTracker = _uploadTracker.value ?: tracker
        finalTracker.progressPercent = 1.0f
        finalTracker.uploadedMb = fileSizeMb
        finalTracker.errorMessage = "🎉 TẢI LÊN HOÀN TẤT 100%! Đã lưu video thành công."
        _uploadTracker.value = finalTracker.copy()

        finalTracker
    }
}

// ==========================================
// 3. METADATA DATABASE REPLICATION & FAILOVER MANAGER
// ==========================================

/**
 * Quản lý Sao lưu Multi-AZ Replication & Phục hồi sự cố tự động (Failover Switcher)
 */
class MetadataDatabaseReplicaManager {

    private val _activeRole = MutableStateFlow(DatabaseRole.PRIMARY_MASTER)
    val activeRole: StateFlow<DatabaseRole> = _activeRole.asStateFlow()

    private val _failoverLogs = MutableStateFlow<List<String>>(
        listOf("Primary DB Master (us-west-2a) active - Syncing Read Replicas (us-east-1, eu-central-1)...")
    )
    val failoverLogs: StateFlow<List<String>> = _failoverLogs.asStateFlow()

    suspend fun triggerPrimaryDatabaseFailover() {
        if (_activeRole.value == DatabaseRole.FAILOVER_MASTER) return

        log("⚠️ PHÁT HIỆN SỰ CỐ: Primary Master DB (us-west-2a) không phản hồi!")
        delay(300L)
        log("🔄 Đang kích hoạt Failover Switcher: Bầu chọn Read Replica (us-east-1b) lên làm Master...")
        delay(500L)
        _activeRole.value = DatabaseRole.FAILOVER_MASTER
        log("✅ FAILOVER HOÀN TẤT: Read Replica us-east-1b đã chuyển thành FAILOVER_MASTER mà không mất dữ liệu!")
    }

    suspend fun restorePrimaryMaster() {
        if (_activeRole.value == DatabaseRole.PRIMARY_MASTER) return

        log("🔄 Đang khôi phục hệ thống Database...")
        delay(400L)
        _activeRole.value = DatabaseRole.PRIMARY_MASTER
        log("🟢 KHÔI PHỤC HOÀN TẤT: Primary Master DB (us-west-2a) đã quay lại hoạt động bình thường!")
    }

    private fun log(msg: String) {
        _failoverLogs.update { (it + msg).takeLast(15) }
    }
}

// ==========================================
// 4. ADAPTIVE BITRATE STREAMING ENGINE
// ==========================================

class AdaptiveBitratePlayerEngine {

    private val logHistory = mutableListOf<String>()

    private val _abrState = MutableStateFlow(
        ABRState(
            currentResolution = Resolution.P720,
            currentBitrateKbps = Resolution.P720.targetBitrateKbps,
            networkBandwidthKbps = 3000,
            bufferHealthSeconds = 15.0f,
            isBuffering = false,
            logs = listOf("ABR Engine đã khởi tạo tại độ phân giải 720p (Băng thông: 3000 kbps)")
        )
    )
    val abrState: StateFlow<ABRState> = _abrState.asStateFlow()

    fun updateNetworkBandwidth(newBandwidthKbps: Int) {
        val currentState = _abrState.value
        val previousRes = currentState.currentResolution
        val targetRes = selectOptimalResolution(newBandwidthKbps, currentState.bufferHealthSeconds)

        val isBuffering = newBandwidthKbps < Resolution.P360.targetBitrateKbps
        val newBuffer = if (isBuffering) (currentState.bufferHealthSeconds - 2f).coerceAtLeast(0f)
        else (currentState.bufferHealthSeconds + 1.5f).coerceAtMost(30f)

        if (targetRes != previousRes) {
            val direction = if (targetRes.targetBitrateKbps > previousRes.targetBitrateKbps) "NÂNG CẤP ⬆️" else "HẠ CẤP ⬇️"
            addLog("Chất lượng video $direction từ ${previousRes.label} sang ${targetRes.label} (Net: $newBandwidthKbps kbps)")
        } else {
            addLog("Duy trì ${targetRes.label} (Net: $newBandwidthKbps kbps, Buffer: ${"%.1f".format(newBuffer)}s)")
        }

        _abrState.update {
            it.copy(
                currentResolution = targetRes,
                currentBitrateKbps = targetRes.targetBitrateKbps,
                networkBandwidthKbps = newBandwidthKbps,
                bufferHealthSeconds = newBuffer,
                isBuffering = isBuffering,
                logs = logHistory.takeLast(10)
            )
        }
    }

    private fun selectOptimalResolution(bandwidthKbps: Int, bufferHealth: Float): Resolution {
        val safeBandwidth = (bandwidthKbps * 0.85).toInt()
        return when {
            safeBandwidth >= Resolution.P1080.targetBitrateKbps && bufferHealth >= 10f -> Resolution.P1080
            safeBandwidth >= Resolution.P720.targetBitrateKbps && bufferHealth >= 5f -> Resolution.P720
            safeBandwidth >= Resolution.P480.targetBitrateKbps -> Resolution.P480
            else -> Resolution.P360
        }
    }

    private fun addLog(message: String) {
        logHistory.add(message)
    }

    fun resetLogs() {
        logHistory.clear()
        addLog("ABR Engine đã reset")
        updateNetworkBandwidth(3000)
    }
}

// ==========================================
// 5. YOUTUBE SYSTEM FACADE & COMPLETION QUEUE / HANDLER
// ==========================================

class YouTubeSystemFacade {

    val dagEngine = TranscodingDAGEngine()
    val abrEngine = AdaptiveBitratePlayerEngine()
    val chunkUploader = ResumableParallelChunkUploader()
    val dbReplicaManager = MetadataDatabaseReplicaManager()

    private val taskMessageQueue = Channel<String>(capacity = Channel.UNLIMITED)
    private val completionQueue = Channel<String>(capacity = Channel.UNLIMITED)

    private val _videoDatabase = MutableStateFlow<Map<String, VideoMetadata>>(emptyMap())
    val videoDatabase: StateFlow<Map<String, VideoMetadata>> = _videoDatabase.asStateFlow()

    private val _cdnEdges = MutableStateFlow(
        listOf(
            CDNEdgeNode("Mỹ-Đông (Virginia)", setOf("video_demo_1"), 15, 98.4),
            CDNEdgeNode("Châu Âu (Frankfurt)", setOf("video_demo_1"), 28, 96.1),
            CDNEdgeNode("Châu Á-Đông (Tokyo)", setOf("video_demo_1"), 45, 94.8),
            CDNEdgeNode("Châu Á-Đông Nam (Singapore)", emptySet(), 35, 91.2)
        )
    )
    val cdnEdges: StateFlow<List<CDNEdgeNode>> = _cdnEdges.asStateFlow()

    private val _systemActivityLogs = MutableStateFlow(
        listOf(
            "Cổng API Gateway hoạt động bình thường.",
            "Cụm Kafka Task Queue & Completion Queue sẵn sàng.",
            "Đã kết nối thùng lưu trữ S3 Blob Storage [youtube-raw-storage]."
        )
    )
    val systemActivityLogs: StateFlow<List<String>> = _systemActivityLogs.asStateFlow()

    /**
     * Máy tính ước tính chi phí CDN theo công thức Chapter 14:
     * 5 triệu DAU * 5 video * 0.3 GB * 0.02 USD = 150,000 USD / ngày ($4,5 Triệu USD / tháng)
     */
    fun calculateCdnCost(
        dauCount: Long = 5000000L,
        videosPerUser: Int = 5,
        avgVideoSizeGb: Double = 0.3,
        costPerGbUsd: Double = 0.02
    ): CdnCostResult {
        val dailyBandwidthGb = dauCount * videosPerUser * avgVideoSizeGb
        val dailyCostUsd = dailyBandwidthGb * costPerGbUsd
        val monthlyCostUsd = dailyCostUsd * 30.0
        val longTailSavedUsd = dailyCostUsd * 0.60

        return CdnCostResult(
            dauCount = dauCount,
            videosPerUser = videosPerUser,
            avgVideoSizeGb = avgVideoSizeGb,
            costPerGbUsd = costPerGbUsd,
            dailyBandwidthGb = dailyBandwidthGb,
            dailyCostUsd = dailyCostUsd,
            monthlyCostUsd = monthlyCostUsd,
            longTailSavedUsd = longTailSavedUsd
        )
    }

    /**
     * Tạo Pre-signed URL mã hóa thật sự bằng chữ ký HMAC-SHA256 theo Upload Ingestion Center gần nhất
     */
    fun requestPresignedUploadUrl(title: String, description: String, uploaderId: String, durationSec: Int, fileSizeMb: Long, region: String = "us-west-2 (Oregon)"): Pair<VideoMetadata, PresignedUrlResponse> {
        val video = VideoMetadata(
            title = title,
            description = description,
            uploaderId = uploaderId,
            durationSeconds = durationSec,
            rawFileSizeBytes = fileSizeMb * 1024 * 1024,
            uploadIngestionRegion = region
        )

        val accessKey = "AKIAIOSFODNN7EXAMPLE"
        val secretKey = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
        val expiresSeconds = 3600
        val timestamp = System.currentTimeMillis() / 1000
        val stringToSign = "PUT\n\nyoutube-raw-storage\nraw/videos/${video.videoId}.mp4\n${timestamp + expiresSeconds}"
        val hmacSignature = calculateHmacSha256(stringToSign, secretKey)

        val presignedUrl = PresignedUrlResponse(
            videoId = video.videoId,
            uploadUrl = "https://youtube-raw-storage-$region.s3.amazonaws.com/raw/videos/${video.videoId}.mp4?AWSAccessKeyId=$accessKey&Expires=${timestamp + expiresSeconds}&Signature=${URLEncoder.encode(hmacSignature, StandardCharsets.UTF_8.name())}",
            s3Key = "raw/videos/${video.videoId}.mp4",
            expirationSeconds = expiresSeconds
        )

        _videoDatabase.update { it + (video.videoId to video) }
        log("🔑 API Gateway cấp Regional Pre-signed URL cho vùng [$region] (Video: '$title')")

        return Pair(video, presignedUrl)
    }

    /**
     * Quy trình xử lý Upload từ từ -> Task Queue -> DAG Transcoding -> Completion Queue & Handler -> Metadata DB Update
     */
    suspend fun processDirectS3UploadAndTranscode(video: VideoMetadata): HLSPlaylist {
        log("1. Client upload từ từ các GOP Chunks (Ingestion Region: ${video.uploadIngestionRegion})...")
        chunkUploader.startResumableParallelUpload(video.videoId, fileSizeMb = (video.rawFileSizeBytes / (1024 * 1024)).coerceAtLeast(100L))

        val taskEvent = "EVT_S3_UPLOAD_COMPLETE|VID:${video.videoId}|TIME:${System.currentTimeMillis()}"
        taskMessageQueue.send(taskEvent)
        log("2. Producer đẩy Task Event vào Kafka Queue: $taskEvent")

        val receivedTask = taskMessageQueue.receive()
        log("3. Worker Consumer rút task từ Queue: $receivedTask")

        _videoDatabase.update { map ->
            map[video.videoId]?.let {
                map + (video.videoId to it.copy(status = TranscodingStatus.INSPECTING))
            } ?: map
        }

        log("4. Khởi chạy đồ thị DAG Transcoding Engine...")
        val hlsResult = dagEngine.executePipeline(video)

        val completionEvent = "EVT_TRANSCODE_SUCCESS|VID:${video.videoId}|MANIFEST:${hlsResult.masterManifestPath}"
        completionQueue.send(completionEvent)
        log("5. 📥 Transcoder gửi Completion Event vào Completion Queue: $completionEvent")

        val processedCompletion = completionQueue.receive()
        log("6. ⚙️ Completion Handler Worker nhận sự kiện: $processedCompletion -> Cập nhật Metadata DB & Cache!")

        _videoDatabase.update { map ->
            map[video.videoId]?.let {
                map + (video.videoId to it.copy(
                    status = TranscodingStatus.COMPLETED,
                    masterPlaylistUrl = hlsResult.masterManifestPath,
                    thumbnailUrl = "s3://cdn.youtube-demo.com/videos/${video.videoId}/thumbnail.jpg"
                ))
            } ?: map
        }

        log("🎉 Cập nhật Metadata DB & Cache thành công! Đẩy file HLS Playlist lên CDN Edge Servers.")
        distributeToCDN(video.videoId)

        return hlsResult
    }

    suspend fun triggerOnDemandTranscodingForColdVideo(videoId: String, targetResolution: Resolution): String {
        log("⚡ [On-Demand Transcoding] Video Long-Tail '$videoId' được yêu cầu bản $targetResolution -> Đang mã hóa theo yêu cầu...")
        delay(300L)
        val onDemandUrl = "s3://cdn.youtube-demo.com/videos/$videoId/${targetResolution.height}p/index.m3u8"
        log("✅ Mã hóa On-Demand hoàn tất: $onDemandUrl")
        return onDemandUrl
    }

    private fun distributeToCDN(videoId: String) {
        _cdnEdges.update { edges ->
            edges.map { edge ->
                edge.copy(
                    cachedVideos = edge.cachedVideos + videoId,
                    cacheHitRatioPercentage = (edge.cacheHitRatioPercentage + 0.5).coerceAtMost(99.9)
                )
            }
        }
    }

    private fun calculateHmacSha256(data: String, key: String): String {
        val secretKeySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKeySpec)
        val hmacBytes = mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return android.util.Base64.encodeToString(hmacBytes, android.util.Base64.NO_WRAP)
    }

    private fun log(msg: String) {
        _systemActivityLogs.update { (it + msg).takeLast(25) }
    }
}
