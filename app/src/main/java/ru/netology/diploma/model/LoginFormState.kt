package ru.netology.diploma.model

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val passwordError: Int? = null,
    val isDataValid: Boolean = false,
    val errorAuth: Boolean = false,
    val errorRegistration: Boolean = false
)