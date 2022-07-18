package ru.netology.diploma.dto

data class EventListModel(
    val event: Event,
    val usersLikeAvatars: List<String?>,
    val speakersNames: List<String?>,
    val usersParticipantsAvatars: List<String?>
)
