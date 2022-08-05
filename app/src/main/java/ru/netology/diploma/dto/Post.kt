package ru.netology.diploma.dto

data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    val published: String,
    /**
     * Координаты
     */
    val coords: Coordinates? = null,
    /**
     * Ссылка на связанный ресурс, например:
     * 1. событие (/events/{id})
     * 2. пользователя (/users/{id})
     * 3. другой пост (/posts/{id})
     * 4. внешний контент (https://youtube.com и т.д.)
     * 5. и т.д.
     */
    val link: String? = null,
    /**
     * Id'шники тех людей/компаний, которые упоминаются в посте (чтобы можно было перейти в их профили)
     */
    val mentionIds: Set<Long> = emptySet(),
    /**
     * Упомянули ли меня
     */
    val mentionedMe: Boolean = false,
    /**
     * Id'шники залайкавших
     */
    val likeOwnerIds: Set<Long> = emptySet(),
    /**
     * Залайкал ли я
     */
    val likedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
    val usersLikeAvatars: List<String?>? = null,
    val mentorsNames: List<String?>? = null,
    val jobs: List<String?>? = null
) : FeedItem {
    companion object {
        val empty = Post(
            id = 0,
            authorId = 0,
            author = "",
            authorAvatar = "",
            content = "",
            published = "2021-08-17T16:46:58.887547Z"
        )
    }
}
