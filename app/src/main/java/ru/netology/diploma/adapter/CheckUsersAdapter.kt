package ru.netology.diploma.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.diploma.databinding.CardCheckUserBinding
import ru.netology.diploma.dto.User
import ru.netology.diploma.utils.Utils

interface OnUserCheckListener {
    fun onCheckUser(id: Long)
    fun isCheckboxVisible(user: User): Boolean
    fun isCheckboxChecked(user: User): Boolean
}


class CheckUsersAdapter(
    private val onUserCheckListener: OnUserCheckListener
) : ListAdapter<User, CheckUsersAdapter.CheckUsersViewHolder>(UserDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckUsersViewHolder {
        val binding =
            CardCheckUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return CheckUsersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CheckUsersViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onUserCheckListener)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }


    class CheckUsersViewHolder(
        private val binding: CardCheckUserBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User, checkListener: OnUserCheckListener) {
            binding.apply {
                userName.text = user.name
                checked.isVisible = checkListener.isCheckboxVisible(user)
                checked.isChecked = checkListener.isCheckboxChecked(user)
                Utils.uploadingAvatar(avatarUser, user.avatar)
            }

            binding.checked.setOnClickListener {
                checkListener.onCheckUser(user.id)
            }
        }
    }
}