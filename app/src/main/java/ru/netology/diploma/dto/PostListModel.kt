package ru.netology.diploma.dto

data class PostListModel(
    val post: Post,
    val userLikeAvatars: List<String?>,
    val mentorNames: List<String?>,
    val jobsAuthor: List<Job?>
)
