package ru.netology.diploma.model

import ru.netology.diploma.dto.Post
import ru.netology.diploma.enumeration.RetryType

data class PostsModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
    val retryType: RetryType? = null,
    val retryId: Long = 0,
    val retryPost: Post? = null,
)