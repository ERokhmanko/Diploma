package ru.netology.diploma.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.paging.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.diploma.api.ApiService
import ru.netology.diploma.dao.PostDao
import ru.netology.diploma.dao.PostRemoteKeyDao
import ru.netology.diploma.dao.PostWorkDao
import ru.netology.diploma.db.AppDb
import ru.netology.diploma.dto.Attachment
import ru.netology.diploma.dto.Media
import ru.netology.diploma.dto.MediaUpload
import ru.netology.diploma.dto.Post
import ru.netology.diploma.entity.PostEntity
import ru.netology.diploma.entity.PostWorkEntity
import ru.netology.diploma.entity.toEntity
import ru.netology.diploma.enumeration.AttachmentType
import ru.netology.diploma.error.ApiError
import ru.netology.diploma.error.AppError
import ru.netology.diploma.error.NetworkError
import ru.netology.diploma.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val apiService: ApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
    private val postWorkDao: PostWorkDao,
    @ApplicationContext private val context: Context
) {

    @OptIn(ExperimentalPagingApi::class)
    val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false,
        ),
        remoteMediator = PostRemoteMediator(apiService, postDao, postRemoteKeyDao, appDb),
        pagingSourceFactory = {
            postDao.getPagingSource()
        }
    ).flow
        .map {
            it.map(PostEntity::toDto).map { post ->

                coroutineScope {
                    val usersId = post.likeOwnerIds + post.mentionIds

                    val usersAsync = async { getUsers(usersId) }
                    val jobsAsync = async { getJobName(post.authorId) }
                    val users = usersAsync.await()
                    val jobs = jobsAsync.await()
                    post.copy(
                        usersLikeAvatars = users.filter { user ->
                            post.likeOwnerIds.contains(user?.id)
                        }.map { user ->
                            user?.avatar
                        },
                        mentorsNames = users.filter { user ->
                            post.mentionIds.contains(user?.id)
                        }.map { user ->
                            user?.name
                        },
                        jobs = jobs
                    )
                }
            }
        }


    @OptIn(ExperimentalPagingApi::class)
    fun userWall(userId: Long): Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        remoteMediator = WallRemoteMediator(apiService, postDao, postRemoteKeyDao, appDb, userId),
        pagingSourceFactory = { postDao.getPagingSource(userId) },
    )
        .flow
        .map {
            it.map(PostEntity::toDto).map { post ->

                coroutineScope {
                    val usersId = post.likeOwnerIds + post.mentionIds

                    val usersAsync = async { getUsers(usersId) }
                    val jobsAsync = async { getJobName(post.authorId) }
                    val users = usersAsync.await()
                    val jobs = jobsAsync.await()
                    post.copy(
                        usersLikeAvatars = users.filter { user ->
                            post.likeOwnerIds.contains(user?.id)
                        }.map { user ->
                            user?.avatar
                        },
                        mentorsNames = users.filter { user ->
                            post.mentionIds.contains(user?.id)
                        }.map { user ->
                            user?.name
                        },
                        jobs = jobs
                    )
                }
            }
        }

    suspend fun getLatest() {
        try {
            val response = apiService.getLatest(10)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw Exception()
            postDao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun getJobName(id: Long) =
        try {
            apiService.getJobs(id).body()?.map {
                it.name
            }
        } catch (e: IOException) {
            throw NetworkError
        }

    private suspend fun getUsers(listId: Set<Long>) =
        try {
            listId.map {
                val response = apiService.getUserById(it)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                response.body()
            }
        } catch (e: IOException) {
            throw NetworkError
        }

    suspend fun saveWork(post: Post, upload: MediaUpload?, type: AttachmentType?): Long {
        try {
            val entity = PostWorkEntity.fromDto(post).apply {
                if (upload != null) {
                    this.uri = upload.uri.toString()
                    this.typeMedia = type?.name.toString()
                }
            }
            return postWorkDao.insert(entity)
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun removeById(id: Long) {
        try {
            postDao.removeById(id)
            val response = apiService.removePostById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun processWork(id: Long) {
        try {
            val entity = postWorkDao.getById(id)
            val post = entity.toDto()
            val uri = entity.uri?.toUri()
            val type = when (entity.typeMedia) {
                "IMAGE" -> AttachmentType.IMAGE
                "AUDIO" -> AttachmentType.AUDIO
                "VIDEO" -> AttachmentType.VIDEO
                else -> {
                    null
                }
            }
            if (uri != null && type != null) {
                saveWithAttachment(post, uri, type)
            } else {
                save(post)
            }
            postWorkDao.removeById(id)
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun save(post: Post) {
        try {
            val response = apiService.savePost(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun saveWithAttachment(post: Post, uri: Uri, type: AttachmentType) {
        try {
            val media = if(!uri.toString().contains("http")) upload(uri).url else uri.toString()

            val postWithAttachment = post.copy(
                attachment = Attachment(
                    url = media,
                    type = type
                )
            )
            save(postWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    private suspend fun upload(uri: Uri): Media {
        try {
            val contentProvider = context.contentResolver

            val body = withContext(Dispatchers.IO) {
                contentProvider?.openInputStream(uri)?.readBytes()
            }?.toRequestBody("*/*".toMediaType()) ?: error("File not found")

            val media = MultipartBody.Part.createFormData(
                "file", "name", body
            )

            val response = apiService.upload(media)


            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    suspend fun likedPostById(id: Long) {
        try {
            postDao.likedById(id)
            val response = apiService.likedPostById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun unlikedPostById(id: Long) {
        try {
            postDao.unlikedById(id)
            val response = apiService.unlikedPostById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


}
