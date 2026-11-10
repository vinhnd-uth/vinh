package com.example.tuan4.data.remote

import com.example.tuan4.data.model.CreateTaskRequest
import com.example.tuan4.data.model.Task
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface TaskApiService {

    @GET("mock/teach/tasks")
    suspend fun getTasks(): List<Task>

    @GET("mock/teach/tasks/{id}")
    suspend fun getTaskById(@Path("id") id: Int): Task

    @POST("mock/teach/tasks")
    suspend fun createTask(@Body task: CreateTaskRequest): Task

    @PATCH("mock/teach/tasks/{id}")
    suspend fun updateTask(@Path("id") id: Int, @Body task: Task): Task

    @DELETE("mock/teach/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int): Response<Unit>

    companion object {
        private const val BASE_URL = "https://share.tryapps.store/"

        fun create(): TaskApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TaskApiService::class.java)
        }
    }
}
