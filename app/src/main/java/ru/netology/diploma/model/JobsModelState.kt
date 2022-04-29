package ru.netology.diploma.model

import ru.netology.diploma.dto.Job
import ru.netology.diploma.dto.User
import ru.netology.diploma.enumeration.RetryType

data class JobModel(
    val jobs: List<Job> = emptyList(),
    val empty: Boolean = false
)

data class JobsModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val retryType: RetryType? = null,
    val retryId: Long = 0,
)