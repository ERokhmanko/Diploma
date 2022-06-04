package ru.netology.diploma.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import okio.utf8Size
import ru.netology.diploma.R
import ru.netology.diploma.databinding.CardAdBinding
import ru.netology.diploma.databinding.CardPostBinding
import ru.netology.diploma.dto.*
import ru.netology.diploma.enumeration.AttachmentType
import ru.netology.diploma.extensions.load
import ru.netology.diploma.model.UserModel
import ru.netology.diploma.utils.Utils
import ru.netology.diploma.utils.Utils.formatDate

interface PostCallback {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun remove(post: Post) {}
    fun edit(post: Post) {}
    fun hide(post: Post) {}
    fun onVideo(post: Post)
    fun onRepost(post: Post)
    fun onAudio(post: Post)
    fun onlikeOwner(post: Post)
    fun onMentors(post: Post)
}

class PostsAdapter(
    private val postCallback: PostCallback,
    private val users: LiveData<UserModel>,
    private val jobs: LiveData<List<Job>>
) :
    PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostsDiffCallback()) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            is Event -> R.layout.card_event
            null -> error("Unknown item type")
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, postCallback, users, jobs)
            }
            R.layout.card_ad -> {
                val binding =
                    CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }
            else -> error("Unknown view type $viewType")
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            else -> error("Unknown view type")
        }
    }

}

class AdViewHolder(
    private val binding: CardAdBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(ad: Ad) {
        binding.imageAd.load(ad.name)

    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val postCallback: PostCallback,
    private val users: LiveData<UserModel>,
    private val jobs: LiveData<List<Job>>
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {

        with(binding) {
            author.text = post.author
            content.text = post.content
            published.text = formatDate(post.published)
            likeCount.text = Utils.reductionInNumbers(post.likeOwnerIds.size)
            like.isChecked = post.likedByMe
            groupLike.visibility =
                if (!post.likeOwnerIds.isNullOrEmpty()) View.VISIBLE else View.GONE


            //TODO разобраться от куда тянуть юзеров, лайв дата тянет только после загрузки страницы юзеров
            val userLike = mutableListOf<String?>()
            val nameMentorsList = mutableListOf<String>()

                users.value?.users?.map { user ->
                    post.likeOwnerIds.map { id ->
                        if (user.id == id) userLike.add(user.avatar)
                    }
                    post.mentionIds.map { id ->
                        if (user.id == id) nameMentorsList.add(user.name)
                    }
                }

            mentors.isVisible = nameMentorsList.isNotEmpty()
            mentorsEdit.isVisible = nameMentorsList.isNotEmpty()

            val nameMentorsString = Utils.listToString(nameMentorsList)
            mentorsEdit.text = nameMentorsString
            moreMentors.isVisible = nameMentorsString.utf8Size() > 36

            when {
                post.likeOwnerIds.isNullOrEmpty() -> {
                    groupLike.visibility = View.GONE
                    cardViewSecondLike.visibility = View.GONE
                }
                post.likeOwnerIds.size == 1 -> {
                    groupLike.visibility = View.VISIBLE
                    if (userLike.isNotEmpty()) {
                        val firstAvatar = userLike.first()
                        Utils.uploadingAvatar(
                            firstLike,
                            firstAvatar
                        )
                    }
                }
                else -> {
                    groupLike.visibility = View.VISIBLE
                    cardViewSecondLike.visibility = View.VISIBLE
                    if (userLike.isNotEmpty()) {
                        val firstAvatar = userLike.first()
                        val secondAvatar = userLike[1]
                        Utils.uploadingAvatar(firstLike, firstAvatar)
                        Utils.uploadingAvatar(
                            secondLike,
                            secondAvatar
                        )
                    }
                }
            }

            if (jobs.value?.isEmpty() == false) placeWork.text = jobs.value!!.first().name //TODO сейчас подтягиваются для всех юзеров работы авторизованного юзера

            Utils.uploadingAvatar(avatar, post.authorAvatar)


            when (post.attachment?.type) {
                AttachmentType.IMAGE -> {
                    Glide.with(imageView)
                        .load(post.attachment.url)
                        .timeout(10_000)
                        .into(imageView)
                }
                AttachmentType.VIDEO -> {
                    // TODO загрузка видео
                }
                AttachmentType.AUDIO -> {
                    // TODO загрузка аудио
                }
            }
            imageView.isVisible = post.attachment?.type == AttachmentType.IMAGE
            groupMedia.isVisible = post.attachment?.type == AttachmentType.VIDEO
            groupAudio.isVisible = post.attachment?.type == AttachmentType.AUDIO

            like.setOnClickListener {
                postCallback.onLike(post)
            }

            repost.setOnClickListener {
                postCallback.onRepost(post)
            }

            share.setOnClickListener {
                postCallback.onShare(post)
            }

            playVideo.setOnClickListener {
                postCallback.onVideo(post)
            }

            playPauseAudio.setOnClickListener {
                postCallback.onAudio(post)
            }

            headerIconLike.setOnClickListener {
                postCallback.onlikeOwner(post)
            }

            firstLike.setOnClickListener {
                postCallback.onlikeOwner(post)
            }

            secondLike.setOnClickListener {
                postCallback.onlikeOwner(post)
            }

            moreMentors.setOnClickListener {
                postCallback.onMentors(post)
            }

            mentorsEdit.setOnClickListener {
                postCallback.onMentors(post)
            }

            menu.setOnClickListener { view ->
                PopupMenu(view.context, view).apply {
                    inflate(R.menu.object_options)
                    menu.let {
                        it.setGroupVisible(R.id.my_object_menu, post.ownedByMe)
                        it.setGroupVisible(R.id.other_object_menu, !post.ownedByMe)
                    }
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.object_remove -> {
                                postCallback.remove(post)
                                true
                            }
                            R.id.object_edit -> {
                                postCallback.edit(post)
                                true
                            }
                            R.id.object_hide -> {
                                postCallback.hide(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }

        }
    }
}


class PostsDiffCallback : DiffUtil.ItemCallback<FeedItem>() {

    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) return false
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }

}
