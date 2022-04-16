package ru.netology.diploma.repository

import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.diploma.api.ApiService
import ru.netology.diploma.dao.PostDao
import ru.netology.diploma.dao.PostRemoteKeyDao
import ru.netology.diploma.db.AppDb
import ru.netology.diploma.dto.Post
import ru.netology.diploma.entity.PostEntity
import ru.netology.diploma.entity.toEntity
import ru.netology.diploma.error.ApiError
import ru.netology.diploma.error.NetworkError
import ru.netology.diploma.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val apiService: ApiService,
    private val postDao: PostDao,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb
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

    suspend fun getAllPosts() {
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

}
