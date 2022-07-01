package ru.netology.diploma.model

import ru.netology.diploma.enumeration.RetryType


data class JobsModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val retryType: RetryType? = null,
    val retryId: Long = 0,
    val linkError: Int? = null,
    val isDataValid: Boolean = false,
    val emptyToDate:Int? = null,
    val isDataNotBlank: Boolean = false,
    val emptyPositionError: Int? = null,
    val emptyCompanyError: Int? = null
)