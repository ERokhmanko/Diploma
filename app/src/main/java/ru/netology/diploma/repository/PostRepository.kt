package ru.netology.diploma.repository

import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
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
        .map { it.map(PostEntity::toDto) }


    @OptIn(ExperimentalPagingApi::class)
    fun userWall(userId: Long): Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        remoteMediator = WallRemoteMediator(apiService, postDao, postRemoteKeyDao, appDb, userId),
        pagingSourceFactory = { postDao.getPagingSource(userId) },
    )
        .flow
        .map { it.map(PostEntity::toDto) }

    suspend fun getAll() {
        try {
            val response = apiService.getAllPosts()
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

    suspend fun saveWork(post: Post, upload: MediaUpload?): Long {
        try {
            val entity = PostWorkEntity.fromDto(post).apply {
                if (upload != null) {
                    this.uri = upload.file.toUri().toString()
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
            if (entity.uri != null) {
                val upload = MediaUpload(Uri.parse(entity.uri).toFile())
                    saveWithAttachment(post, upload)
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

    private suspend fun saveWithAttachment(post: Post, upload: MediaUpload, ) {
        try {
            val media = upload(upload)

            val postWithAttachment = post.copy(
                        attachment = Attachment(
                            url = media.id,
                            type =AttachmentType.IMAGE
                        )
                    ) //TODO изменить для друких вложений

            save(postWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    private suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file",
                upload.file.name,
                upload.file.asRequestBody()
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
