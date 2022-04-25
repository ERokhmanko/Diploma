package ru.netology.diploma.model

import ru.netology.diploma.dto.User

data class UserModel(
    val users: List<User> = emptyList(),
    val empty: Boolean = false
)

data class UsersModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
)