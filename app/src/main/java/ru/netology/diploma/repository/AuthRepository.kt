package ru.netology.diploma.repository

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.diploma.api.ApiService
import ru.netology.diploma.dto.AuthState
import ru.netology.diploma.dto.MediaUpload
import ru.netology.diploma.error.ApiError
import ru.netology.diploma.error.NetworkError
import ru.netology.diploma.error.UnknownError
import java.io.IOException
import javax.inject.Inject

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun authUser(login: String, password: String): AuthState {
        try {
            val response = apiService.authUser(login, password)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw Exception()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

    }

    suspend fun registrationUserWithAvatar(
        login: String,
        password: String,
        name: String,
        file: MediaUpload
    ): AuthState {
        try {
            val media = MultipartBody.Part.createFormData(
                "file",
                file.file.name,
                file.file.asRequestBody()
            )

            val response = apiService.registrationUserWithAvatar(
                login.toRequestBody("text/plain".toMediaType()),
                password.toRequestBody("text/plain".toMediaType()),
                name.toRequestBody("text/plain".toMediaType()),
                media
            )
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw Exception()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError()
        }
    }

    suspend fun registrationUser(login: String, password: String, name: String): AuthState {
        try {
            val response = apiService.registrationUser(login, password, name)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw Exception()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError()
        }
    }
}