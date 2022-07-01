package ru.netology.diploma.model

import ru.netology.diploma.dto.Event
import ru.netology.diploma.enumeration.RetryType

data class EventModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
    val retryType: RetryType? = null,
    val retryId: Long = 0,
    val retryEvent: Event? = null,
)