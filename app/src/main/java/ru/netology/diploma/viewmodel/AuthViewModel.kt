package ru.netology.diploma.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.diploma.R
import ru.netology.diploma.auth.AppAuth
import ru.netology.diploma.dto.AuthState
import ru.netology.diploma.dto.MediaUpload
import ru.netology.diploma.model.FileModel
import ru.netology.diploma.model.LoginFormState
import ru.netology.diploma.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor
    (
    private val appAuth: AppAuth,
    private val authRepository: AuthRepository
) : ViewModel() {

    val authenticated: Boolean
        get() = appAuth.authStateFlow.value.id != 0L

    private val _data = MutableLiveData<AuthState>()
    val data: LiveData<AuthState>
        get() = _data

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState>
        get() = _loginForm

    private val noPhoto = FileModel()
    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<FileModel>
        get() = _photo

    fun authUser(login: String, password: String) {
        viewModelScope.launch {
            try {
                val user = authRepository.authUser(login, password)
                _data.value = user
            } catch (e: Exception) {
                _loginForm.postValue(LoginFormState(errorAuth = true))
            }
        }

    }

    fun registrationUser(login: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                val user = _photo.value?.uri?.let {
                    MediaUpload(it)
                }
                    .run {
                        if (this == null) authRepository.registrationUser(login, password, name)
                        else authRepository.registrationUserWithAvatar(login, password, name, this)
                    }
                _data.value = user
            } catch (e: Exception) {
                _loginForm.postValue(LoginFormState(errorRegistration = true))
            }
        }
    }

    fun loginDataChanged(password: String) {
        if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }


    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 5
    }

    fun changePhoto(uri: Uri?) {
        _photo.value = FileModel(uri)
    }
}
