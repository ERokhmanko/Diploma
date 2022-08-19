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
import ru.netology.diploma.dao.EventDao
import ru.netology.diploma.dao.EventRemoteKeyDao
import ru.netology.diploma.dao.EventWorkDao
import ru.netology.diploma.db.AppDb
import ru.netology.diploma.dto.Attachment
import ru.netology.diploma.dto.Event
import ru.netology.diploma.dto.Media
import ru.netology.diploma.dto.MediaUpload
import ru.netology.diploma.entity.EventEntity
import ru.netology.diploma.entity.EventWorkEntity
import ru.netology.diploma.entity.toEntity
import ru.netology.diploma.enumeration.AttachmentType
import ru.netology.diploma.error.ApiError
import ru.netology.diploma.error.AppError
import ru.netology.diploma.error.NetworkError
import ru.netology.diploma.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class EventRepository @Inject constructor(
    private val apiService: ApiService,
    private val eventDao: EventDao,
    eventRemoteKeyDao: EventRemoteKeyDao,
    appDb: AppDb,
    private val eventWorkDao: EventWorkDao,
    @ApplicationContext private val context: Context

) {

    @OptIn(ExperimentalPagingApi::class)
    val data: Flow<PagingData<Event>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false,
        ),
        remoteMediator = EventRemoteMediator(apiService, eventDao, eventRemoteKeyDao, appDb),
        pagingSourceFactory = {
            eventDao.getPagingSource()
        }
    ).flow
        .map {
            it.map(EventEntity::toDto).map { event ->

                coroutineScope {
                    val usersId = event.likeOwnerIds + event.speakerIds + event.participantsIds

                    val usersAsync = async { getUsers(usersId) }
                    val users = usersAsync.await()
                    event.copy(
                        usersLikeAvatars = users.filter { user ->
                            event.likeOwnerIds.contains(user?.id)
                        }.map { user ->
                            user?.avatar
                        },
                        speakersNames = users.filter { user ->
                            event.speakerIds.contains(user?.id)
                        }.map { user ->
                            user?.name
                        },
                        usersParticipantsAvatars = users.filter { user ->
                            event.participantsIds.contains(user?.id)
                        }.map { user ->
                            user?.avatar
                        }
                    )
                }
            }
        }

    suspend fun getLatest() {
        try {
            val response = apiService.getLatestEvents(10)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw Exception()
            eventDao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
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

    suspend fun saveWork(event: Event, upload: MediaUpload?, type: AttachmentType?): Long {
        try {
            val entity = EventWorkEntity.fromDto(event).apply {
                if (upload != null) {
                    this.uri = upload.uri.toString()
                    this.typeMedia = type?.name.toString()
                }
            }
            return eventWorkDao.insert(entity)
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun removeById(id: Long) {
        try {
            eventDao.removeById(id)
            val response = apiService.removeEventById(id)
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
            val entity = eventWorkDao.getById(id)
            val event = entity.toDto()
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
                saveWithAttachment(event, uri, type)
            } else {
                save(event)
            }
            eventWorkDao.removeById(id)
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun save(event: Event) {
        try {
            val response = apiService.saveEvent(event)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun saveWithAttachment(event: Event, uri: Uri, type: AttachmentType) {
        try {
            val media = if(!uri.toString().contains("http")) upload(uri).url else uri.toString()

            val eventWithAttachment = event.copy(
                attachment = Attachment(
                    url = media,
                    type = type
                )
            )
            save(eventWithAttachment)
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

    suspend fun likedEventById(id: Long) {
        try {
            eventDao.likedById(id)
            val response = apiService.likedEventById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun unlikedEventById(id: Long) {
        try {
            eventDao.unlikedById(id)
            val response = apiService.unlikedEventById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun participateById(id: Long) {
        try {
            eventDao.participateById(id)
            val response = apiService.participateById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun unParticipateById(id: Long) {
        try {
            eventDao.unParticipateById(id)
            val response = apiService.unParticipateById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

}
