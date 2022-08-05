package ru.netology.diploma.repository

import ru.netology.diploma.api.ApiService
import ru.netology.diploma.dao.JobDao
import ru.netology.diploma.dao.JobWorkDao
import ru.netology.diploma.dto.Job
import ru.netology.diploma.entity.JobEntity
import ru.netology.diploma.entity.JobWorkEntity
import ru.netology.diploma.entity.toEntity
import ru.netology.diploma.error.ApiError
import ru.netology.diploma.error.NetworkError
import ru.netology.diploma.error.UnknownError
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.diploma.entity.toDto

class JobRepository @Inject constructor(
    private val apiService: ApiService,
    private val jobDao: JobDao,
    private val jobWorkDao: JobWorkDao
) {

    val data: Flow<List<Job>> = jobDao.getAll()
        .map { it.toDto() }
        .flowOn(Dispatchers.Default)

    suspend fun getJobsByUserId(id: Long) {
        try {
            jobDao.removeAll()
            val response = apiService.getJobs(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val data = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(data.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: java.lang.Exception) {
            throw UnknownError()
        }
    }

    suspend fun removeById(id: Long) {
        try {
            val response = apiService.removeJobById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            jobDao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun saveWork(job: Job): Long {
        try {
            val entity = JobWorkEntity.fromDto(job)
            return jobWorkDao.insert(entity)
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun processWork(id: Long) {
        try {
            val entity = jobWorkDao.getById(id)
            val job = entity.toDto()
                save(job)
            jobWorkDao.removeById(id)
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun save(job: Job) {
        try {
            val response = apiService.saveJob(job)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(JobEntity.fromDto(body))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

}
