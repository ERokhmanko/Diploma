package ru.netology.diploma.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.diploma.dto.Event
import ru.netology.diploma.enumeration.EventType

@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    val datetime: String,
    val published: String,
    @Embedded
    val coords: CoordinatesEmbeddable?,
    @Embedded
    val type: EventTypeEmbeddable,
    val likeOwnerIds: Set<Long> = emptySet(),
    val likedByMe: Boolean = false,
    val speakerIds: MutableSet<Long> = mutableSetOf(),
    val participantsIds: Set<Long> = emptySet(),
    val participatedByMe: Boolean = false,
    @Embedded
    val attachment: AttachmentEmbeddable? = null,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    val usersLikeAvatars: List<String?>? = null,
    val speakersNames: List<String?>? = null,
    val usersParticipantsAvatars: List<String?>? = null

) {
    fun toDto() = Event(
        id, authorId, author, authorAvatar,
        content, datetime,
        published,
        coords?.toDto(), type.toDto(),
        likeOwnerIds,
        likedByMe, speakerIds, participantsIds, participatedByMe,
        attachment?.toDto(), link,
        ownedByMe,
        usersLikeAvatars,
        speakersNames,
        usersParticipantsAvatars
    )

    companion object {
        fun fromDto(event: Event) = EventEntity(
            event.id, event.authorId, event.author, event.authorAvatar,
            event.content, event.datetime,
            event.published,
            CoordinatesEmbeddable.fromDto(event.coords), EventTypeEmbeddable.fromDto(event.type),
            event.likeOwnerIds,
            event.likedByMe, event.speakerIds, event.participantsIds, event.participatedByMe,
            AttachmentEmbeddable.fromDto(event.attachment), event.link,
            event.ownedByMe,
            event.usersLikeAvatars,
            event.speakersNames,
            event.usersParticipantsAvatars
        )
    }
}

data class EventTypeEmbeddable(
    val eventType: String
) {
    fun toDto() = EventType.valueOf(eventType)

    companion object {
        fun fromDto(dto: EventType) = EventTypeEmbeddable(dto.name)
    }

}


fun List<EventEntity>.toDto() = map(EventEntity::toDto)
fun List<Event>.toEntity() = map(EventEntity::fromDto)

