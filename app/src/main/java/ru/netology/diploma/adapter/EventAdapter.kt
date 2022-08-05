package ru.netology.diploma.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import okio.utf8Size
import ru.netology.diploma.R
import ru.netology.diploma.databinding.CardAdBinding
import ru.netology.diploma.databinding.CardEventBinding
import ru.netology.diploma.dto.*
import ru.netology.diploma.enumeration.AttachmentType
import ru.netology.diploma.enumeration.EventType
import ru.netology.diploma.utils.Utils
import ru.netology.diploma.utils.Utils.formatDate

interface EventCallback {
    fun onLike(event: Event)
    fun onShare(event: Event)
    fun onJoin(event: Event)
    fun remove(event: Event)
    fun edit(event: Event)
    fun hide(event: Event)
    fun onVideo(event: Event)
    fun onAudio(event: Event)
    fun onLink(event: Event)
    fun onSpeakers(event: Event)
    fun onLikeOwner(event: Event)
    fun onParticipants(event: Event)
    fun onMap(event: Event)
}

class EventsAdapter(
    private val eventCallback: EventCallback,
) :
    PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(EventsDiffCallback()) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Event -> R.layout.card_event
            is Post -> R.layout.card_post
            null -> error("Unknown item type")

        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_event -> {
                val binding =
                    CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                EventViewHolder(binding, eventCallback)
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
            is Event -> (holder as? EventViewHolder)?.bind(item)
            else -> error("Unknown view type")
        }
    }

}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val eventCallback: EventCallback,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(event: Event) {

        with(binding) {
            Utils.uploadingAvatar(avatar, event.authorAvatar)
            author.text = event.author
            published.text = formatDate(event.published)
            content.text = event.content
            datetimeEdit.text =
                formatDate(event.datetime)
            eventTypeEdit.text = event.type.toString()
            link.isVisible = event.type == EventType.ONLINE
            coordinates.isVisible = event.type == EventType.OFFLINE

            val userLike = event.usersLikeAvatars ?: emptyList()
            val nameSpeakersList = event.speakersNames ?: emptyList()
            val avatarParticipants = event.usersParticipantsAvatars ?: emptyList()

            speakers.isVisible = nameSpeakersList.isNotEmpty()
            speakersEdit.isVisible = nameSpeakersList.isNotEmpty()

            val nameSpeakersString = Utils.listToString(nameSpeakersList)
            speakersEdit.text = nameSpeakersString
            moreSpeakers.isVisible = nameSpeakersString.utf8Size() > 18


            likeCount.text = Utils.reductionInNumbers(event.likeOwnerIds.size)

            like.isChecked = event.likedByMe
            join.isChecked = event.participatedByMe

            groupLike.visibility =
                if (!event.likeOwnerIds.isNullOrEmpty()) View.VISIBLE else View.GONE

            when {
                event.likeOwnerIds.isNullOrEmpty() -> {
                    groupLike.visibility = View.GONE
                    cardViewSecondLike.visibility = View.GONE
                }
                event.likeOwnerIds.size == 1 -> {
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

            participantCount.text = Utils.reductionInNumbers(event.participantsIds.size)

            groupParticipant.visibility =
                if (!event.participantsIds.isNullOrEmpty()) View.VISIBLE else View.GONE

            when {
                event.participantsIds.isNullOrEmpty() -> {
                    groupParticipant.visibility = View.GONE
                }
                event.participantsIds.size == 1 -> {
                    groupParticipant.visibility = View.VISIBLE
                    if (avatarParticipants.isNotEmpty()) {
                        val firstAvatar = avatarParticipants.first()
                        Utils.uploadingAvatar(firstParticipant, firstAvatar)
                    }
                }
                else -> {
                    groupParticipant.visibility = View.VISIBLE
                    cardViewSecondParticipant.visibility = View.VISIBLE
                    if (avatarParticipants.isNotEmpty()) {
                        val firstAvatar = avatarParticipants.first()
                        val secondAvatar = avatarParticipants[1]
                        Utils.uploadingAvatar(firstLike, firstAvatar)
                        Utils.uploadingAvatar(secondLike, secondAvatar)
                    }
                }
            }

            when (event.attachment?.type) {
                AttachmentType.IMAGE -> {
                    Utils.uploadingMedia(imageView, event.attachment.url)
                }
                AttachmentType.VIDEO -> {
                    Utils.uploadingMedia(videoView, event.attachment.url)
                }
            }

            imageView.isVisible = event.attachment?.type == AttachmentType.IMAGE
            groupMedia.isVisible = event.attachment?.type == AttachmentType.VIDEO
            groupAudio.isVisible = event.attachment?.type == AttachmentType.AUDIO



            link.setOnClickListener {
                eventCallback.onLink(event)
            }

            coordinates.setOnClickListener {
                eventCallback.onMap(event)
            }

            like.setOnClickListener {
                eventCallback.onLike(event)
            }

            join.setOnClickListener {
                eventCallback.onJoin(event)
            }

            share.setOnClickListener {
                eventCallback.onShare(event)
            }

            playVideo.setOnClickListener {
                eventCallback.onVideo(event)
            }

            playPauseAudio.setOnClickListener {
                eventCallback.onAudio(event)
            }

            moreSpeakers.setOnClickListener {
                eventCallback.onSpeakers(event)
            }

            speakersEdit.setOnClickListener {
                eventCallback.onSpeakers(event)
            }

            headerIconLike.setOnClickListener {
                eventCallback.onLikeOwner(event)
            }

            firstLike.setOnClickListener {
                eventCallback.onLikeOwner(event)
            }

            secondLike.setOnClickListener {
                eventCallback.onLikeOwner(event)
            }

            headerIconParticipants.setOnClickListener {
                eventCallback.onParticipants(event)
            }

            firstParticipant.setOnClickListener {
                eventCallback.onParticipants(event)
            }

            secondParticipant.setOnClickListener {
                eventCallback.onParticipants(event)
            }

            menu.setOnClickListener { view ->
                PopupMenu(view.context, view).apply {
                    inflate(R.menu.object_options)
                    menu.let {
                        it.setGroupVisible(R.id.my_object_menu, event.ownedByMe)
                        it.setGroupVisible(R.id.other_object_menu, !event.ownedByMe)
                    }
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.object_remove -> {
                                eventCallback.remove(event)
                                true
                            }
                            R.id.object_edit -> {
                                eventCallback.edit(event)
                                true
                            }
                            R.id.object_hide -> {
                                eventCallback.hide(event)
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


class EventsDiffCallback : DiffUtil.ItemCallback<FeedItem>() {

    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) return false
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }

}
