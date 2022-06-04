package ru.netology.diploma.repository

import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.diploma.api.ApiService
import ru.netology.diploma.dao.JobDao
import ru.netology.diploma.dao.JobWorkDao
import ru.netology.diploma.dao.PostWorkDao
import ru.netology.diploma.dto.Job
import ru.netology.diploma.dto.MediaUpload
import ru.netology.diploma.dto.Post
import ru.netology.diploma.entity.*
import ru.netology.diploma.error.ApiError
import ru.netology.diploma.error.NetworkError
import ru.netology.diploma.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class JobRepository @Inject constructor(
    private val apiService: ApiService,
    private val jobDao: JobDao,
    private val jobWorkDao: JobWorkDao
) {

    suspend fun getJobsByUserId(userId: Long) : List<Job> {
        try {
            val response = apiService.getJobs(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(body.toEntity())
            return body
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    suspend fun removeById(id: Long) {
        try {
            jobDao.removeById(id)
            val response = apiService.removeJobById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

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

    suspend fun getMyJobs() {
            try {
                val response = apiService.getMyJobs()
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw Exception()
                jobDao.insert(body.toEntity())
            } catch (e: IOException) {
                throw NetworkError
            } catch (e: Exception) {
                throw UnknownError
            }
    }

}
