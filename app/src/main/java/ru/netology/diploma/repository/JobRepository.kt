package ru.netology.diploma.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.diploma.api.ApiService
import ru.netology.diploma.dao.JobDao
import ru.netology.diploma.dto.Job
import ru.netology.diploma.entity.toDto
import ru.netology.diploma.entity.toEntity
import ru.netology.diploma.error.ApiError
import ru.netology.diploma.error.NetworkError
import ru.netology.diploma.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class JobRepository @Inject constructor(
    private val apiService: ApiService,
    private val jobDao: JobDao,
) {
    val data: Flow<List<Job>> = jobDao.getAll().map {
        it.toDto()
    }.flowOn(Dispatchers.Default)




    suspend fun getJobsByUserId(userId: Long) : List<Job> {
        try {
            val response = apiService.getJobs(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
//            jobDao.insert(body.toEntity())
            return body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

}
