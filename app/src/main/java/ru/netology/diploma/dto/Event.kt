package ru.netology.diploma.dto

import ru.netology.diploma.enumeration.EventType

data class Event(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    /**
     * Дата и время проведения
     */
    val datetime: String,
    val published: String,
    /**
     * Координаты проведения
     */
    val coords: Coordinates? = null,
    /**
     * Типы события
     */
    val type: EventType,
    /**
     * Id'шники залайкавших
     */
    val likeOwnerIds: Set<Long> = emptySet(),
    /**
     * Залайкал ли я
     */
    val likedByMe: Boolean = false,
    /**
     * Id'шники спикеров
     */
    val speakerIds: MutableSet<Long> = mutableSetOf(),
    /**
     * Id'шники участников
     */
    val participantsIds: Set<Long> = emptySet(),
    /**
     * Участвовал ли я
     */
    val participatedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    val usersLikeAvatars: List<String?>? = null,
    val speakersNames: List<String?>? = null,
    val usersParticipantsAvatars: List<String?>? = null
) : FeedItem {
    companion object {
        val empty = Event(
            id = 0,
            authorId = 0,
            author = "",
            authorAvatar = "",
            content = "",
            datetime = "2021-08-17T16:46:58.887547Z",
            published ="2021-08-17T16:46:58.887547Z",
            type = EventType.ONLINE,
        )
    }
}
