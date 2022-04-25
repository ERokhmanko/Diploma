package ru.netology.diploma.model

import ru.netology.diploma.dto.Job
import ru.netology.diploma.dto.User

data class JobModel(
    val jobs: List<Job> = emptyList(),
    val empty: Boolean = false
)

data class JobsModelState(
    val loading: Boolean = false,
    val error: Boolean = false
)