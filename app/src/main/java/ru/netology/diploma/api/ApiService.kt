package ru.netology.diploma.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.diploma.dto.*


interface ApiService {
    //User
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

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<User>

    @GET("users")
    suspend fun getAllUsers(): Response<List<User>>


    //Posts
    @GET("posts")
    suspend fun getAllPosts(): Response<List<Post>>

    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/before")
    suspend fun getBefore(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getAfter(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    @DELETE("posts/{id}")
    suspend fun removePostById(@Path("id") id: Long): Response<Unit>

    @POST("posts")
    suspend fun savePost(@Body post: Post): Response<Post>

    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>

    //Walls
    @GET("{userId}/wall/{id}/before")
    suspend fun getWallBefore(
        @Path("userId") userId: Long,
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("{userId}/wall/{id}/after")
    suspend fun getWallAfter(
        @Path("userId") userId: Long,
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("{userId}/wall/latest")
    suspend fun getWallLatest(
        @Path("userId") userId: Long,
        @Query("count") count: Int
    ): Response<List<Post>>


    //Jobs
    @GET("{id}/jobs")
    suspend fun getJobs(@Path("id") id: Long): Response<List<Job>>

    @GET("my/jobs")
    suspend fun getMyJobs(): Response<List<Job>>

    @POST("my/jobs")
    suspend fun saveJob(@Body job: Job): Response<Job>

    @DELETE("my/jobs/{id}")
    suspend fun removeJobById(@Path("id") id: Long): Response<Unit>




}

