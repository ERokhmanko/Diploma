package ru.netology.diploma.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.diploma.databinding.CardUserBinding
import ru.netology.diploma.dto.User
import ru.netology.diploma.extensions.addAllOnClickListener
import ru.netology.diploma.utils.Utils.uploadingAvatar

interface UserCallback {
    fun onUser(user: User)
}

class UsersAdapter(private val userCallback: UserCallback) :
    ListAdapter<User, UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding =
            CardUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, userCallback)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }

}


class UserViewHolder(private val binding: CardUserBinding, private val userCallback: UserCallback) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(user: User) {

        with(binding) {
            userName.text = user.name
            uploadingAvatar(avatarUser, user.avatar)

            groupUser.addAllOnClickListener {
               userCallback.onUser(user)
            }
        }
    }
}

class UserDiffCallback  : DiffUtil.ItemCallback<User>() {

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        if (oldItem::class != newItem::class) return false
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }

}
