package ru.netology.diploma.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.diploma.auth.AppAuth
import ru.netology.diploma.dto.User
import ru.netology.diploma.model.UserModel
import ru.netology.diploma.model.UsersModelState
import ru.netology.diploma.repository.UserRepository
import ru.netology.diploma.ui.USER_ID
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository,
//    private val stateHandle: SavedStateHandle,
    private val appAuth: AppAuth
) : ViewModel() {

    val data: LiveData<UserModel> = repository.data
        .map { user ->
            UserModel(
                user,
                user.isEmpty()
            )
        }.asLiveData(Dispatchers.Default)
    val id = appAuth.authStateFlow.value.id
    val user = MutableLiveData<User>()

    private val _dataState = MutableLiveData<UsersModelState>()
    val dataState: LiveData<UsersModelState>
        get() = _dataState

    init {
        getUserById(id)
//        getUserById(stateHandle.get(USER_ID) ?: 0)
    }


    private fun getUserById(id: Long) = viewModelScope.launch {
        try {
            _dataState.value = UsersModelState(loading = true)
            user.value = repository.getUserById(id)
            _dataState.value = UsersModelState()
        } catch (e: Exception) {
            _dataState.value = UsersModelState(error = true)
        }
    }
}

