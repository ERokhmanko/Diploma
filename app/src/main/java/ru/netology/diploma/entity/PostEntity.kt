package ru.netology.diploma.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.diploma.dto.Attachment
import ru.netology.diploma.dto.Coordinates
import ru.netology.diploma.dto.Post
import ru.netology.diploma.enumeration.AttachmentType

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    val published: String,
    @Embedded
    val coords: CoordinatesEmbeddable?,
    val link: String? = null,
    var mentionIds: Set<Long> = emptySet(),
    val mentionedMe: Boolean = false,
    val likeOwnerIds: Set<Long> = emptySet(),
    val likedByMe: Boolean = false,
    @Embedded
    val attachment: AttachmentEmbeddable? = null,
    val ownedByMe: Boolean = false,
    val usersLikeAvatars: List<String?>? = null,
    val mentorsNames: List<String?>? = null,
    val jobs: List<String?>? = null
) {
    fun toDto() = Post(
        id, authorId, author, authorAvatar,
        content,
        published,
        coords?.toDto(),
        link,
        mentionIds,
        mentionedMe,
        likeOwnerIds,
        likedByMe,
        attachment?.toDto(),
        ownedByMe,
        usersLikeAvatars,
        mentorsNames, jobs
    )

    companion object {
        fun fromDto(post: Post) = PostEntity(
            post.id, post.authorId, post.author, post.authorAvatar,
            post.content,
            post.published,
            CoordinatesEmbeddable.fromDto(post.coords),
            post.link,
            post.mentionIds,
            post.mentionedMe,
            post.likeOwnerIds,
            post.likedByMe,
            AttachmentEmbeddable.fromDto(post.attachment),
            post.ownedByMe,
            post.usersLikeAvatars,
            post.mentorsNames,
            post.jobs
        )
    }
}

data class CoordinatesEmbeddable(
    val lat: Double,
    val longitude: Double,
) {
    fun toDto() = Coordinates(lat, longitude)

    companion object {
        fun fromDto(dto: Coordinates?) = dto?.let {
            CoordinatesEmbeddable(it.lat, it.long)
        }
    }
}

data class AttachmentEmbeddable(
    var url: String,
    var type: AttachmentType,
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}

fun List<PostEntity>.toDto() = map(PostEntity::toDto)
fun List<Post>.toEntity() = map(PostEntity::fromDto)

