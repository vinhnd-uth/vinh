package com.example.demo.youtube

import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ==========================================
// 1. CÁC ENUM VÀ MÔ HÌNH DỮ LIỆU (DOMAINS & MODELS)
// ==========================================

/**
 * Các tab chuyển đổi giao diện ứng dụng YouTube Mobile
 */
enum class YouTubeTab {
    HOME,             // Trang chủ
    SHORTS,           // Video ngắn Shorts
    UPLOAD,           // Đăng video mới (YouTube Studio)
    SUBSCRIPTIONS,    // Kênh đăng ký
    YOU,              // Thư viện & Kênh cá nhân
    SYSTEM_DESIGN     // Bảng điều khiển kiến trúc hệ thống Chapter 14
}

/**
 * Danh sách độ phân giải video hỗ trợ trong luồng HLS/DASH (Chapter 14)
 */
enum class Resolution(val label: String, val height: Int, val targetBitrateKbps: Int) {
    P1080("1080p (Full HD)", 1080, 5000),
    P720("720p (HD)", 720, 2500),
    P480("480p (SD)", 480, 1000),
    P360("360p (Low)", 360, 500)
}

/**
 * Phân phối nguồn phát video theo kiến trúc Long-Tail Distribution
 */
enum class ServingOrigin(val label: String, val description: String) {
    CDN_EDGE("Global CDN Edge Node", "Dành cho 20% video hot (chiếm 80% lượt xem) - Tốc độ cao, độ trễ thấp"),
    ORIGIN_STORAGE("S3 Blob Storage Origin", "Dành cho 80% video ít xem (Long-Tail) - Tiết kiệm chi phí CDN")
}

/**
 * Phương thức bảo vệ bản quyền video (DRM & Mã Hóa HLS)
 */
enum class DRMScheme(val label: String, val encryptionMethod: String) {
    AES_128("Mã hóa HLS AES-128", "AES-128 Key Exchange"),
    WIDEVINE_L1("Google Widevine L1", "Hardware DRM Encryption"),
    FAIRPLAY("Apple FairPlay DRM", "Apple HLS DRM License"),
    PLAYREADY("Microsoft PlayReady", "PlayReady DRM License")
}

/**
 * Trạng thái phân vai Database Master/Replica (Replication & Failover)
 */
enum class DatabaseRole(val label: String) {
    PRIMARY_MASTER("Primary Write Master (Multi-AZ)"),
    READ_REPLICA("Read-Only Replica (Region East)"),
    FAILOVER_MASTER("Promoted Primary Master (Post-Failover)")
}

/**
 * Trạng thái tiến trình mã hóa video trong hệ thống
 */
enum class TranscodingStatus {
    IDLE,         // Chờ xử lý
    INSPECTING,   // Kiểm tra file video & container
    SPLITTING,    // Cắt video thành các đoạn 10 giây (GOP Chunks)
    TRANSCODING,  // Mã hóa các độ phân giải song song
    PACKAGING,    // Đóng gói file Master HLS (.m3u8)
    COMPLETED,    // Hoàn thành mã hóa
    FAILED        // Mã hóa thất bại
}

/**
 * Các loại tác vụ xử lý trong đồ thị DAG (Directed Acyclic Graph)
 */
enum class DAGNodeType {
    INSPECT,          // Kiểm tra file
    SPLIT,            // Cắt video
    TRANSCODE_VIDEO,  // Mã hóa video
    TRANSCODE_AUDIO,  // Mã hóa âm thanh AAC
    WATERMARK,        // Chèn logo bản quyền
    THUMBNAIL,        // Tạo ảnh thumbnail tự động
    HLS_PACKAGE       // Đóng gói HLS Manifest
}

/**
 * Trạng thái thực thi của từng node trong đồ thị DAG
 */
enum class NodeExecutionState {
    PENDING,  // Đang chờ nút phụ thuộc hoàn thành
    RUNNING,  // Đang chạy
    SUCCESS,  // Thành công
    FAILED    // Thất bại
}

/**
 * Các tùy chọn nhận thông báo từ kênh đăng ký
 */
enum class NotificationOption(val label: String, val icon: String) {
    ALL("Tất cả thông báo", "🔔"),
    PERSONALIZED("Dành cho bạn", "🔕"),
    NONE("Không thông báo", "🚫")
}

/**
 * Nút tác vụ xử lý trong đồ thị DAG
 */
data class DAGNode(
    val id: String,
    val name: String,
    val type: DAGNodeType,
    val dependencies: List<String> = emptyList(),
    var state: NodeExecutionState = NodeExecutionState.PENDING,
    var progress: Float = 0f,
    var detailMessage: String = ""
)

/**
 * Thông tin chi tiết của file video gốc
 */
data class VideoMetadata(
    val videoId: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val uploaderId: String,
    val durationSeconds: Int,
    val rawFileSizeBytes: Long,
    var status: TranscodingStatus = TranscodingStatus.IDLE,
    var masterPlaylistUrl: String? = null,
    var thumbnailUrl: String? = null,
    val isHotVideo: Boolean = true,
    val servingOrigin: ServingOrigin = ServingOrigin.CDN_EDGE,
    val drmScheme: DRMScheme = DRMScheme.AES_128,
    val uploadIngestionRegion: String = "us-west-2 (Oregon)"
)

/**
 * Kết quả tính toán chi phí CDN theo công thức Chapter 14
 */
data class CdnCostResult(
    val dauCount: Long,
    val videosPerUser: Int,
    val avgVideoSizeGb: Double,
    val costPerGbUsd: Double,
    val dailyBandwidthGb: Double,
    val dailyCostUsd: Double,
    val monthlyCostUsd: Double,
    val longTailSavedUsd: Double
)

/**
 * Theo dõi tiến trình Upload song song theo GOP Chunks & Resume khi rớt mạng
 */
data class ResumableUploadTracker(
    val videoId: String,
    val totalChunks: Int = 8,
    val completedChunkIndices: MutableSet<Int> = mutableSetOf(),
    var progressPercent: Float = 0f,
    var uploadedMb: Long = 0L,
    var totalMb: Long = 350L,
    var isOnline: Boolean = true,
    var isInterrupted: Boolean = false,
    var isResumed: Boolean = false,
    var uploadSpeedMbps: Double = 48.5,
    var errorMessage: String = ""
)

/**
 * Kết quả cấp đường link Pre-signed URL từ S3 Blob Storage
 */
data class PresignedUrlResponse(
    val videoId: String,
    val uploadUrl: String,
    val s3Key: String,
    val expirationSeconds: Int = 3600
)

/**
 * Thông tin file danh sách phát HLS Master Manifest
 */
data class HLSPlaylist(
    val videoId: String,
    val masterManifestPath: String,
    val variantStreams: Map<Resolution, String>,
    val totalSegments: Int
)

/**
 * Trạng thái node mạng phân phối nội dung CDN (Content Delivery Network)
 */
data class CDNEdgeNode(
    val region: String,
    val cachedVideos: Set<String>,
    val latencyMs: Int,
    var cacheHitRatioPercentage: Double
)

/**
 * Trạng thái thuật toán phát video tự động thích ứng băng thông ABR (Adaptive Bitrate)
 */
data class ABRState(
    val currentResolution: Resolution,
    val currentBitrateKbps: Int,
    val networkBandwidthKbps: Int,
    val bufferHealthSeconds: Float,
    val isBuffering: Boolean,
    val logs: List<String>
)

/**
 * Danh sách phát video cá nhân (Playlist)
 */
data class Playlist(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val itemCount: Int,
    val isPrivate: Boolean = true
)

/**
 * Thông tin Kênh YouTube
 */
data class Channel(
    val id: String,
    val name: String,
    val handle: String = "@" + name.lowercase().replace(" ", ""),
    val avatarUrl: String,
    val subscribersCount: String,
    var isSubscribed: Boolean = false,
    var notificationOption: NotificationOption = NotificationOption.ALL,
    val hasUnreadVideo: Boolean = true
)

/**
 * Bình luận dưới bài đăng video
 */
data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val authorName: String,
    val authorAvatarUrl: String,
    val text: String,
    val timeAgo: String,
    var likesCount: Int = 0,
    var isLiked: Boolean = false
)

/**
 * Thông tin Tài khoản Người dùng (Authentication User Account)
 */
data class UserAccount(
    val uid: String = UUID.randomUUID().toString(),
    val displayName: String = "Lập Trình Viên Architect",
    val email: String = "architect.dev@gmail.com",
    val avatarUrl: String = "",
    var isSignedIn: Boolean = false,
    val handle: String = "@architect_dev"
)

/**
 * Chi tiết đối tượng Video phát trên ứng dụng YouTube Client
 */
data class VideoItem(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val duration: String,
    val durationSeconds: Int = 300,
    var watchedSeconds: Int = 0,
    val videoUrl: String,
    val channel: Channel,
    val views: String,
    val publishedTimeAgo: String,
    var likesCount: Int,
    var dislikesCount: Int = 120,
    var isLiked: Boolean = false,
    var isDisliked: Boolean = false,
    var isSaved: Boolean = false,
    var isDownloaded: Boolean = false,
    val category: String = "Tất cả",
    val isHotVideo: Boolean = true,
    val servingOrigin: ServingOrigin = ServingOrigin.CDN_EDGE,
    val drmScheme: DRMScheme = DRMScheme.AES_128,
    val comments: MutableList<Comment> = mutableListOf()
)

// ==========================================
// 2. KHO DỮ LIỆU MẪU VÀ BỘ MÁY TÌM KIẾM THÔNG MINH (MOCK REPOSITORY & SEARCH)
// ==========================================

object MockYouTubeRepository {

    // Danh sách các kênh YouTube đăng ký mẫu
    val sampleChannels = mutableListOf(
        Channel("c1", "System Design Academy", "@systemdesign", "", "1.2M người đăng ký"),
        Channel("c2", "Android Developers", "@androiddevs", "", "2.8M người đăng ký", isSubscribed = true),
        Channel("c3", "Tech World VN", "@techworldvn", "", "450K người đăng ký"),
        Channel("c4", "Lofi Girl", "@lofigirl", "", "14M người đăng ký", isSubscribed = true),
        Channel("c5", "Game Zone Official", "@gamezone", "", "890K người đăng ký"),
        Channel("c6", "UI/UX Masterclass", "@uiuxmaster", "", "320K người đăng ký")
    )

    // Danh mục phân loại video
    val sampleCategories = listOf("Tất cả", "Lập trình", "Công nghệ", "Âm nhạc", "Trực tiếp", "Trò chơi", "Đời sống", "Thiết kế")

    // Từ khóa tìm kiếm gợi ý gần đây
    val searchSuggestions = listOf(
        "System Design Interview Chapter 14",
        "Jetpack Compose Material 3 Tutorial",
        "YouTube Architecture DAG Transcoding",
        "Adaptive Bitrate Streaming HLS",
        "Kotlin Coroutines & Flow",
        "Đánh giá Smartphone 2026",
        "Lofi Hip Hop Radio Study Beats",
        "Tối ưu hiệu năng Android App"
    )

    // Danh sách các video phong phú khởi tạo ban đầu
    val initialVideos = listOf(
        VideoItem(
            id = "v1",
            title = "System Design Interview – Chapter 14: Design YouTube",
            description = "Phân tích toàn diện kiến trúc hệ thống YouTube: Upload bằng Pre-signed URL, mã hóa video DAG Engine song song, Adaptive Bitrate Streaming (HLS/DASH) và phân phối CDN.",
            thumbnailUrl = "",
            duration = "18:45",
            durationSeconds = 1125,
            watchedSeconds = 340,
            videoUrl = "s3://cdn.youtube-demo.com/videos/v1/master.m3u8",
            channel = sampleChannels[0],
            views = "342K lượt xem",
            publishedTimeAgo = "2 ngày trước",
            likesCount = 18400,
            category = "Lập trình",
            isHotVideo = true,
            servingOrigin = ServingOrigin.CDN_EDGE,
            drmScheme = DRMScheme.AES_128,
            comments = mutableListOf(
                Comment("cm1", "Nguyễn Văn A", "", "Bài viết phân tích DAG Transcoding Engine quá hay và chi tiết!", "1 ngày trước", 124),
                Comment("cm2", "Trần Thị B", "", "Sử dụng Pre-signed URL thực sự giải quyết vấn đề nghẽn băng thông server.", "18 giờ trước", 45),
                Comment("cm3", "Lê Hoàng C", "", "Mô phỏng ABR streaming chuyển 720p lên 1080p mượt lắm ad ơi.", "5 giờ trước", 12)
            )
        ),
        VideoItem(
            id = "v2",
            title = "Xây dựng ứng dụng Android với Jetpack Compose & Material 3",
            description = "Hướng dẫn chi tiết xây dựng giao diện chuẩn Material Design 3 mượt mà với Jetpack Compose mới nhất.",
            thumbnailUrl = "",
            duration = "24:10",
            durationSeconds = 1450,
            watchedSeconds = 1200,
            videoUrl = "s3://cdn.youtube-demo.com/videos/v2/master.m3u8",
            channel = sampleChannels[1],
            views = "1.2M lượt xem",
            publishedTimeAgo = "1 tuần trước",
            likesCount = 54000,
            category = "Lập trình",
            isHotVideo = true,
            servingOrigin = ServingOrigin.CDN_EDGE,
            drmScheme = DRMScheme.WIDEVINE_L1,
            comments = mutableListOf(
                Comment("cm4", "Phạm Đức D", "", "Compose đỉnh cao thật sự, code giao diện cực kỳ nhanh!", "3 ngày trước", 89)
            )
        ),
        VideoItem(
            id = "v3",
            title = "lofi hip hop radio - beats to relax/study to ☕",
            description = "Trực tiếp phát nhạc lofi thư giãn học tập và làm việc 24/7.",
            thumbnailUrl = "",
            duration = "TRỰC TIẾP",
            durationSeconds = 9999,
            watchedSeconds = 450,
            videoUrl = "s3://cdn.youtube-demo.com/videos/v3/master.m3u8",
            channel = sampleChannels[3],
            views = "45K đang xem",
            publishedTimeAgo = "Đang phát trực tiếp",
            likesCount = 210000,
            category = "Âm nhạc",
            isHotVideo = true,
            servingOrigin = ServingOrigin.CDN_EDGE
        ),
        VideoItem(
            id = "v4",
            title = "Đánh giá Smartphone Flagship 2026: Đột phá mới?",
            description = "Trải nghiệm và đánh giá chi tiết thiết kế, camera và hiệu năng chip xử lý AI thế hệ mới.",
            thumbnailUrl = "",
            duration = "12:30",
            durationSeconds = 750,
            watchedSeconds = 0,
            videoUrl = "s3://cdn.youtube-demo.com/videos/v4/master.m3u8",
            channel = sampleChannels[2],
            views = "89K lượt xem",
            publishedTimeAgo = "3 ngày trước",
            likesCount = 4200,
            category = "Công nghệ",
            isHotVideo = false,
            servingOrigin = ServingOrigin.ORIGIN_STORAGE
        ),
        VideoItem(
            id = "v5",
            title = "Hướng dẫn Kotlin Coroutines & Flow nâng cao trong Android",
            description = "Master asynchronous programming với StateFlow, SharedFlow, Dispatchers và exception handling.",
            thumbnailUrl = "",
            duration = "31:15",
            durationSeconds = 1875,
            watchedSeconds = 90,
            videoUrl = "s3://cdn.youtube-demo.com/videos/v5/master.m3u8",
            channel = sampleChannels[1],
            views = "560K lượt xem",
            publishedTimeAgo = "4 ngày trước",
            likesCount = 28000,
            category = "Lập trình",
            isHotVideo = true,
            servingOrigin = ServingOrigin.CDN_EDGE
        ),
        VideoItem(
            id = "v6",
            title = "Top 10 Game Đồ Họa Đỉnh Cao 2026 Cho Mobile & PC",
            description = "Tổng hợp danh sách các tựa game thế giới mở đồ họa Ray Tracing siêu đẹp ra mắt năm 2026.",
            thumbnailUrl = "",
            duration = "15:20",
            durationSeconds = 920,
            watchedSeconds = 0,
            videoUrl = "s3://cdn.youtube-demo.com/videos/v6/master.m3u8",
            channel = sampleChannels[4],
            views = "780K lượt xem",
            publishedTimeAgo = "5 ngày trước",
            likesCount = 41000,
            category = "Trò chơi",
            isHotVideo = false,
            servingOrigin = ServingOrigin.ORIGIN_STORAGE
        ),
        VideoItem(
            id = "v7",
            title = "Bí Quyết Thiết Kế UI/UX Chuẩn YouTube Mobile App",
            description = "Phân tích các nguyên lý thiết kế giao diện ứng dụng di động chuẩn Material Design 3 và thói quen người dùng.",
            thumbnailUrl = "",
            duration = "19:40",
            durationSeconds = 1180,
            watchedSeconds = 400,
            videoUrl = "s3://cdn.youtube-demo.com/videos/v7/master.m3u8",
            channel = sampleChannels[5],
            views = "230K lượt xem",
            publishedTimeAgo = "1 tuần trước",
            likesCount = 15200,
            category = "Thiết kế",
            isHotVideo = false,
            servingOrigin = ServingOrigin.ORIGIN_STORAGE
        ),
        VideoItem(
            id = "v8",
            title = "Nhạc Thư Giãn Sâu Không Lời Giúp Tập Trung Làm Việc 3 Giờ 🌿",
            description = "Tuyển tập nhạc Piano & Guitar nhẹ nhàng giúp giảm căng thẳng và tăng năng suất làm việc.",
            thumbnailUrl = "",
            duration = "03:00:00",
            durationSeconds = 10800,
            watchedSeconds = 1800,
            videoUrl = "s3://cdn.youtube-demo.com/videos/v8/master.m3u8",
            channel = sampleChannels[3],
            views = "3.4M lượt xem",
            publishedTimeAgo = "2 tuần trước",
            likesCount = 160000,
            category = "Âm nhạc",
            isHotVideo = true,
            servingOrigin = ServingOrigin.CDN_EDGE
        ),
        VideoItem(
            id = "v9",
            title = "Kinh Nghiệm Phỏng Vấn Senior Android Developer Tại Các Tập Đoàn Tech",
            description = "Chia sẻ bộ câu hỏi phỏng vấn kiến trúc Android, DI (Hilt/Koin), MVI/MVVM, Kotlin Coroutines & System Design.",
            thumbnailUrl = "",
            duration = "28:50",
            durationSeconds = 1730,
            watchedSeconds = 0,
            videoUrl = "s3://cdn.youtube-demo.com/videos/v9/master.m3u8",
            channel = sampleChannels[0],
            views = "410K lượt xem",
            publishedTimeAgo = "6 ngày trước",
            likesCount = 23500,
            category = "Lập trình",
            isHotVideo = true,
            servingOrigin = ServingOrigin.CDN_EDGE
        ),
        VideoItem(
            id = "v10",
            title = "Cách Tối Ưu Hiệu Năng App Android - Tránh Jank & Lag UI",
            description = "Sử dụng Android Profiler, R8 Optimization, Baseline Profiles và Compose Recomposition Tracing.",
            thumbnailUrl = "",
            duration = "22:15",
            durationSeconds = 1335,
            watchedSeconds = 0,
            videoUrl = "s3://cdn.youtube-demo.com/videos/v10/master.m3u8",
            channel = sampleChannels[1],
            views = "195K lượt xem",
            publishedTimeAgo = "3 ngày trước",
            likesCount = 11200,
            category = "Lập trình",
            isHotVideo = false,
            servingOrigin = ServingOrigin.ORIGIN_STORAGE
        )
    )

    // Luồng StateFlow phát thông tin danh sách video phản ứng
    private val _videos = MutableStateFlow<List<VideoItem>>(initialVideos)
    val videos: StateFlow<List<VideoItem>> = _videos.asStateFlow()

    // Danh sách video Shorts phát dọc phong phú
    val sampleShorts = mutableListOf(
        VideoItem(
            id = "s1",
            title = "Mẹo tối ưu Compose Recomposition cực nhanh #shorts",
            description = "Sử dụng remember & derivedStateOf tối ưu recomposition.",
            thumbnailUrl = "",
            duration = "0:45",
            durationSeconds = 45,
            videoUrl = "s3://cdn.youtube-demo.com/shorts/s1/master.m3u8",
            channel = sampleChannels[0],
            views = "500K lượt xem",
            publishedTimeAgo = "1 ngày trước",
            likesCount = 34000,
            category = "Lập trình",
            comments = mutableListOf(
                Comment("c_s1", "Nguyễn Minh", "", "Mẹo dùng derivedStateOf đỉnh quá!", "2 giờ trước", 45)
            )
        ),
        VideoItem(
            id = "s2",
            title = "Unboxing Smartphone Siêu Độc Lạ 2026 #shorts",
            description = "Mở hộp thiết bị công nghệ màn hình cuộn siêu nhẹ.",
            thumbnailUrl = "",
            duration = "0:30",
            durationSeconds = 30,
            videoUrl = "s3://cdn.youtube-demo.com/shorts/s2/master.m3u8",
            channel = sampleChannels[2],
            views = "1.1M lượt xem",
            publishedTimeAgo = "4 ngày trước",
            likesCount = 98000,
            category = "Công nghệ",
            comments = mutableListOf(
                Comment("c_s2", "Trần Hoàng", "", "Đẹp xuất sắc ad ơi!", "1 ngày trước", 89)
            )
        ),
        VideoItem(
            id = "s3",
            title = "Lofi Chill Beats cho Đêm Khuya Học Bài #shorts",
            description = "Giai điệu lofi piano nhẹ nhàng xua tan căng thẳng.",
            thumbnailUrl = "",
            duration = "0:59",
            durationSeconds = 59,
            videoUrl = "s3://cdn.youtube-demo.com/shorts/s3/master.m3u8",
            channel = sampleChannels[3],
            views = "850K lượt xem",
            publishedTimeAgo = "3 ngày trước",
            likesCount = 67000,
            category = "Âm nhạc"
        ),
        VideoItem(
            id = "s4",
            title = "Pha Xử Lý Game Thủ Pro 1v5 Siêu Đỉnh #shorts",
            description = "High-light highlight clutch không tưởng trong trận chung kết.",
            thumbnailUrl = "",
            duration = "0:40",
            durationSeconds = 40,
            videoUrl = "s3://cdn.youtube-demo.com/shorts/s4/master.m3u8",
            channel = sampleChannels[4],
            views = "2.3M lượt xem",
            publishedTimeAgo = "5 ngày trước",
            likesCount = 180000,
            category = "Trò chơi"
        )
    )

    // Danh sách phát cá nhân phản ứng
    private val _playlists = MutableStateFlow(
        listOf(
            Playlist("p1", "Xem sau (Watch Later)", 6, isPrivate = true),
            Playlist("p2", "Video đã thích", 18, isPrivate = true),
            Playlist("p3", "Học Lập Trình Android 2026", 12, isPrivate = false),
            Playlist("p4", "System Design & Distributed Systems", 20, isPrivate = false)
        )
    )
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    // Danh sách các tài khoản đã lưu trên thiết bị (cho tính năng Chuyển đổi tài khoản)
    private val _savedAccounts = MutableStateFlow(
        listOf(
            UserAccount(
                uid = "u1",
                displayName = "Lập Trình Viên Architect",
                email = "architect.dev@gmail.com",
                isSignedIn = true,
                handle = "@architect_dev"
            ),
            UserAccount(
                uid = "u2",
                displayName = "Tech Reviewer VN",
                email = "tech.reviewer@gmail.com",
                isSignedIn = false,
                handle = "@tech_reviewer"
            ),
            UserAccount(
                uid = "u3",
                displayName = "Gamer Pro Official",
                email = "gamer.pro@gmail.com",
                isSignedIn = false,
                handle = "@gamer_pro"
            )
        )
    )
    val savedAccounts: StateFlow<List<UserAccount>> = _savedAccounts.asStateFlow()

    // Trạng thái tài khoản đăng nhập hiện tại
    private val _currentUser = MutableStateFlow(_savedAccounts.value.first())
    val currentUser: StateFlow<UserAccount> = _currentUser.asStateFlow()

    fun signIn(email: String, name: String) {
        val newAcc = UserAccount(
            displayName = name.ifBlank { "Thành viên YouTube" },
            email = email.ifBlank { "user@youtube.com" },
            isSignedIn = true
        )
        _savedAccounts.update { list ->
            val updated = list.map { it.copy(isSignedIn = false) }
            listOf(newAcc) + updated
        }
        _currentUser.value = newAcc
    }

    fun signUp(email: String, name: String) {
        val newAcc = UserAccount(
            displayName = name.ifBlank { "Thành viên Mới" },
            email = email,
            isSignedIn = true
        )
        _savedAccounts.update { list ->
            val updated = list.map { it.copy(isSignedIn = false) }
            listOf(newAcc) + updated
        }
        _currentUser.value = newAcc
    }

    fun switchAccount(account: UserAccount) {
        _savedAccounts.update { list ->
            list.map { it.copy(isSignedIn = it.uid == account.uid) }
        }
        _currentUser.value = account.copy(isSignedIn = true)
    }

    fun signOut() {
        _savedAccounts.update { list ->
            list.map { it.copy(isSignedIn = false) }
        }
        _currentUser.update { it.copy(isSignedIn = false) }
    }

    fun createPlaylist(name: String, isPrivate: Boolean) {
        _playlists.update { list ->
            list + Playlist(name = name, itemCount = 0, isPrivate = isPrivate)
        }
    }

    fun getVideosForPlaylist(playlist: Playlist): List<VideoItem> {
        val currentList = _videos.value
        return when (playlist.id) {
            "p1" -> currentList.filter { it.isSaved || it.id in listOf("v1", "v2", "v4") }
            "p2" -> currentList.filter { it.isLiked || it.id in listOf("v1", "v3", "v5", "v8") }
            "p3" -> currentList.filter { it.category == "Lập trình" }
            else -> currentList.take(3)
        }
    }

    fun getVideosForChannel(channel: Channel): List<VideoItem> {
        return _videos.value.filter { it.channel.id == channel.id || it.channel.name == channel.name }
    }

    /**
     * Thêm video mới trực tiếp vào danh sách Feed Trang chủ (cho tính năng Upload)
     */
    fun addVideo(video: VideoItem) {
        _videos.update { listOf(video) + it }
    }

    /**
     * Bộ máy tìm kiếm từ khóa thông minh (Smart Tokenized Fuzzy Search Engine)
     * Tự động chuẩn hóa dấu câu và phân tách từ khóa để tìm chính xác video
     */
    fun searchVideos(query: String, category: String, allList: List<VideoItem>): List<VideoItem> {
        return allList.filter { video ->
            val matchesCategory = category == "Tất cả" || video.category == category
            if (!matchesCategory) return@filter false
            if (query.isBlank()) return@filter true

            val cleanQuery = normalize(query)
            val fullText = normalize("${video.title} ${video.channel.name} ${video.description} ${video.category}")

            if (fullText.contains(cleanQuery)) return@filter true

            val queryTokens = cleanQuery.split("\\s+".toRegex()).filter { it.length > 1 }
            if (queryTokens.isEmpty()) return@filter true
            queryTokens.all { token -> fullText.contains(token) }
        }
    }

    /**
     * Hàm chuẩn hóa chuỗi văn bản (loại bỏ ký tự đặc biệt, gạch ngang và đưa về chữ thường)
     */
    private fun normalize(str: String): String {
        return str.lowercase()
            .replace("–", " ")
            .replace("-", " ")
            .replace(":", " ")
            .replace(Regex("[^a-z0-9àáảãạăắằẳẵặâấầẩẫậèéẻẽẹêếềểễệđìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵ\\s]"), "")
            .trim()
    }
}
