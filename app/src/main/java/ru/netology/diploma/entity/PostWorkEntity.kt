package ru.netology.diploma.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.diploma.dto.Post

@Entity
data class PostWorkEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long,
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
    var uri: String? = null,
    var typeMedia: String? = null

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
        ownedByMe
    )

    companion object {
        fun fromDto(post: Post) = PostWorkEntity( 0L,
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
            post.ownedByMe
        )
    }
}


