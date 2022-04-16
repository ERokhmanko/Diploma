package ru.netology.diploma.dto

sealed interface FeedItem {
    val id: Long
}

data class Ad(
    override val id: Long,
    val name: String
) : FeedItem