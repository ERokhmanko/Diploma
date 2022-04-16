package ru.netology.diploma.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.diploma.dto.AuthState
import ru.netology.diploma.dto.Job
import ru.netology.diploma.dto.Post
import ru.netology.diploma.dto.PushToken


interface ApiService {
    @POST("users/push-tokens")
    suspend fun saveToken(@Body pushToken: PushToken): Response<Unit>

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun authUser(
        @Field("login") login: String,
        @Field("pass") pass: String
    ): Response<AuthState>

    @Multipart
    @POST("users/registration")
    suspend fun registrationUserWithAvatar(
        @Part("login") login: RequestBody,
        @Part("pass") pass: RequestBody,
        @Part("name") name: RequestBody,
        @Part media: MultipartBody.Part
    ): Response<AuthState>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun registrationUser(
        @Field("login") login: String?,
        @Field("pass") password: String?,
        @Field("name") name: String
    ): Response<AuthState>

    @GET("posts")
    suspend fun getAllPosts(): Response<List<Post>>

    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/before")
    suspend fun getBefore(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getAfter(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("my/jobs")
    suspend fun getMyJobs(): Response<List<Job>>


}

