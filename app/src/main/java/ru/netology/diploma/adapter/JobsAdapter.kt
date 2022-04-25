package ru.netology.diploma.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.diploma.R
import ru.netology.diploma.databinding.CardJobBinding
import ru.netology.diploma.dto.Job
import java.time.Instant

interface JobCallback {
    fun edit(job: Job)
    fun remove(job: Job)
}

class JobsAdapter(private val jobCallback: JobCallback, private val showPopupMenu: Boolean) :
    ListAdapter<Job, JobViewHolder>(JobDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding =
            CardJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, jobCallback, showPopupMenu)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }

}


class JobViewHolder(
    private val binding: CardJobBinding,
    private val jobCallback: JobCallback,
    private val showPopupMenu: Boolean
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(job: Job) {

        with(binding) {
            workPeriod.text = "${job.start} - ${job.finish}" //TODO исправить
            nameOrganization.text = job.name
            position.text = job.position
            link.text = job.link

            menu.setOnClickListener { view ->
                PopupMenu(view.context, view).apply {
                    inflate(R.menu.post_options) //TODO меню подходит? если да, то надо перезвать
                    menu.let {
                        it.setGroupVisible(R.id.my_post_menu, showPopupMenu)
                    }
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.post_remove -> {
                                jobCallback.remove(job)
                                true
                            }
                            R.id.post_edit -> {
                                jobCallback.edit(job)
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

class JobDiffCallback : DiffUtil.ItemCallback<Job>() {

    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        if (oldItem::class != newItem::class) return false
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }

}
