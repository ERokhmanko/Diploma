package ru.netology.diploma.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
    stateHandle: SavedStateHandle,
    appAuth: AppAuth
) : ViewModel() {

    val data: LiveData<UserModel> = repository.data
        .map { user ->
            UserModel(
                user,
                user.isEmpty()
            )
        }.asLiveData(Dispatchers.Default)

    private var profileId = stateHandle[USER_ID] ?: appAuth.authStateFlow.value.id

    val user = MutableLiveData<User>()

    private val _dataState = MutableLiveData<UsersModelState>()
    val dataState: LiveData<UsersModelState>
        get() = _dataState

    private val _usersIds = MutableLiveData<Set<Long>>()
    val userIds: LiveData<Set<Long>>
        get() = _usersIds

    init {
        getUserById(profileId)
        loadUsers()
    }

    private fun loadUsers() = viewModelScope.launch {
        try {
            _dataState.value = UsersModelState(loading = true)
            repository.getAllUsers()
            _dataState.value = UsersModelState()
        } catch (e: Exception) {
            _dataState.value = UsersModelState(error = true)
        }
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

    fun getUsersIds(set: Set<Long>) = viewModelScope.launch {
        _usersIds.value = set
    }

    fun getUserName(id: Long): String? {
        data.value?.users?.map { user ->
            if (id == user.id) return user.name
        }
        return null
    }

}

