package ru.netology.diploma.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.netology.diploma.api.ApiService
import ru.netology.diploma.dto.Post
import ru.netology.diploma.dto.PostListModel
import ru.netology.diploma.error.ApiError
import ru.netology.diploma.error.NetworkError
import ru.netology.diploma.error.UnknownError
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

class PostEventListRepository @Inject constructor(
    private val apiService: ApiService,
) {

    suspend fun getPosts()=

        try {
            val response = apiService.getAllPosts()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            response.body() ?: throw Exception()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

    suspend fun getEvents()=

        try {
            val response = apiService.getAllEvents()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            response.body() ?: throw Exception()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

    suspend fun getUserAvatars(listId: Set<Long>) =
        try {
            listId.map {
                val response = apiService.getUserById(it)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                response.body()?.avatar

            }

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }


    suspend fun getUsersNames(listId: Set<Long>) =
        try {
            listId.map {
                val response = apiService.getUserById(it)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                response.body()?.name
            }

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

    suspend fun getJobAuthor(id: Long) = try {
        val response = apiService.getJobs(id)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }
        response.body() ?: throw Exception()
    } catch (e: IOException) {
        throw NetworkError
    } catch (e: Exception) {
        e.toString()
        throw UnknownError
    }

}


