package ru.netology.diploma.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.diploma.BuildConfig.BASE_URL
import ru.netology.diploma.R
import ru.netology.diploma.databinding.CardAdBinding
import ru.netology.diploma.databinding.CardPostBinding
import ru.netology.diploma.dto.Ad
import ru.netology.diploma.dto.FeedItem
import ru.netology.diploma.dto.Post
import ru.netology.diploma.dto.User
import ru.netology.diploma.enumeration.AttachmentType
import ru.netology.diploma.extensions.load
import ru.netology.diploma.utils.Utils
import ru.netology.diploma.utils.Utils.formatDate

interface PostCallback {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun remove(post: Post)
    fun edit(post: Post)
    fun hide(post: Post)
    fun onVideo(post: Post)
    fun onRepost(post: Post)
    fun onAudio(post: Post)

}

class PostsAdapter(private val postCallback: PostCallback) :
    PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostsDiffCallback()) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            null -> error("Unknown item type")
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, postCallback)
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
            null -> error("Unknown view type")
        }
    }

}

class AdViewHolder(
    private val binding: CardAdBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(ad: Ad) {
        binding.imageAd.load("$BASE_URL/media/${ad.name}")
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val postCallback: PostCallback
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

            when {
                post.likeOwnerIds.isNullOrEmpty() -> {
                    groupLike.visibility = View.GONE
                }
                post.likeOwnerIds.size == 1 -> {
                    groupLike.visibility = View.VISIBLE
//                    val user = likeOwner.first()
//                    Utils.uploadingAvatar(firstLike, user.avatar) //TODO придумать как подтягивать аватарки лайкнувших
                }
                else -> {
                    groupLike.visibility = View.VISIBLE
                    cardViewSecondLike.visibility = View.VISIBLE
//                    val firstUser = likeOwner.first()
//                    val secondUser = likeOwner[2]
//                    Utils.uploadingAvatar(firstLike, firstUser.avatar)
//                    Utils.uploadingAvatar(secondLike, secondUser.avatar) //TODO придумать как подтягивать аватарки лайкнувших
                }
            }

//            placeWork.text = //TODO подтянуть сюда инфу при работе с ui work

            Utils.uploadingAvatar(avatar, post.authorAvatar)


            when (post.attachment?.type) {
                AttachmentType.IMAGE -> {
                    Glide.with(mediaView)
                        .load("$BASE_URL/media/${post.attachment.url}")
                        .timeout(10_000)
                        .into(mediaView as ImageView)
                }
                AttachmentType.VIDEO -> {
                    // TODO загрузка видео
                }
                AttachmentType.AUDIO -> {
                    // TODO загрузка аудио
                }
            }
            mediaView.isVisible = post.attachment?.type == AttachmentType.IMAGE
            groupMedia.isVisible = post.attachment?.type == AttachmentType.VIDEO
            groupAudio.isVisible = post.attachment?.type == AttachmentType.AUDIO

            if (!post.link.isNullOrBlank()) {
            //TODO придумать реализацию
            }

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

            mediaView.setOnClickListener {
                when (post.attachment?.type) {
                    AttachmentType.VIDEO -> {
                        postCallback.onVideo(post)
                    }
                    AttachmentType.AUDIO -> {
                        postCallback.onAudio(post)
                    }
                }
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
