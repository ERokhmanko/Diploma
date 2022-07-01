package ru.netology.diploma.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.diploma.dto.Event

@Entity
data class EventWorkEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long,
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
    var uri: String? = null,
    var typeMedia: String? = null
) {
    fun toDto() = Event(
        id, authorId, author, authorAvatar,
        content, datetime,
        published,
        coords?.toDto(), type.toDto(),
        likeOwnerIds,
        likedByMe, speakerIds, participantsIds, participatedByMe,
        attachment?.toDto(), link,
        ownedByMe
    )

    companion object {
        fun fromDto(event: Event) = EventWorkEntity(
            0L,
            event.id, event.authorId, event.author, event.authorAvatar,
            event.content, event.datetime,
            event.published,
            CoordinatesEmbeddable.fromDto(event.coords), EventTypeEmbeddable.fromDto(event.type),
            event.likeOwnerIds,
            event.likedByMe, event.speakerIds, event.participantsIds, event.participatedByMe,
            AttachmentEmbeddable.fromDto(event.attachment), event.link,
            event.ownedByMe
        )
    }
}


