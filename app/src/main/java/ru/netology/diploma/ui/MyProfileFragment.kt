package ru.netology.diploma.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.diploma.R
import ru.netology.diploma.adapter.JobCallback
import ru.netology.diploma.adapter.JobsAdapter
import ru.netology.diploma.adapter.PostCallback
import ru.netology.diploma.adapter.PostsAdapter
import ru.netology.diploma.auth.AppAuth
import ru.netology.diploma.databinding.FragmentProfileBinding
import ru.netology.diploma.dto.Event
import ru.netology.diploma.dto.Job
import ru.netology.diploma.dto.Post
import ru.netology.diploma.utils.Utils.uploadingAvatar
import ru.netology.diploma.viewmodel.EventViewModel
import ru.netology.diploma.viewmodel.JobViewModel
import ru.netology.diploma.viewmodel.PostViewModel
import ru.netology.diploma.viewmodel.UserViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MyProfileFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth

    private val postViewModel: PostViewModel by activityViewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val jobViewModel: JobViewModel by viewModels()
    private val eventViewModel: EventViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val bundle = Bundle()
        val binding = FragmentProfileBinding.inflate(inflater, container, false)


        userViewModel.user.observe(viewLifecycleOwner) {
            with(binding) {
                userName.text = it.name
                uploadingAvatar(avatarUser, it.avatar)
            }
        }

        val postsAdapter = PostsAdapter(object : PostCallback {
            override fun onLike(post: Post) {
                if (!post.likedByMe) postViewModel.likeById(post.id) else postViewModel.unlikeById(
                    post.id
                )
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, post.content)
                }

                val shareIntent = Intent.createChooser(intent, getString(R.string.share_post))
                startActivity(shareIntent)
            }

            override fun remove(post: Post) {
                postViewModel.removeById(post.id)
            }

            override fun edit(post: Post) {
                postViewModel.edit(post)
                bundle.putString("content", post.content)
                bundle.putString("attachment", post.attachment?.url)
                bundle.putString("attachmentType", post.attachment?.type?.name.toString())
                findNavController().navigate(
                    R.id.action_navigation_profile_to_newPostFragment,
                    bundle
                )
            }

            override fun hide(post: Post) {
                postViewModel.hidePost(post)
            }

            override fun onVideo(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.attachment?.url))
                val videoIntent = Intent.createChooser(intent, getString(R.string.media_chooser))
                startActivity(videoIntent)
            }

            override fun onMap(post: Post) {
                post.coords?.lat?.let { bundle.putDouble("lat", it) }
                post.coords?.long?.let { bundle.putDouble("lng", it) }
                findNavController().navigate(
                    R.id.action_navigation_my_profile_to_mapFragment,
                    bundle
                )
            }

            override fun onRepost(post: Post) {

                postViewModel.changeContent(post.content, post.coords)
                if (post.attachment != null) {
                    postViewModel.attachmentRepost(post.attachment)
                }
                postViewModel.save()

                val toast = Toast.makeText(
                    context,
                    R.string.repost,
                    Toast.LENGTH_LONG
                )
                toast.show()
            }

            override fun onAudio(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.attachment?.url))
                val audioIntent = Intent.createChooser(intent, getString(R.string.media_chooser))
                startActivity(audioIntent)
            }

            override fun onMentors(post: Post) {
                userViewModel.getUsersIds(post.mentionIds)
                findNavController().navigate(R.id.action_navigation_my_profile_to_usersBottomSheet)
            }

            override fun onLikeOwner(post: Post) {
                userViewModel.getUsersIds(post.likeOwnerIds)
                findNavController().navigate(R.id.action_navigation_my_profile_to_usersBottomSheet)
            }
        })

        binding.listPosts.adapter = postsAdapter

        lifecycleScope.launchWhenCreated {
            postViewModel.userWall.collectLatest {
                postsAdapter.submitData(it)
            }

        }

        val jobsAdapter = JobsAdapter(object : JobCallback {
            override fun edit(job: Job) {
                jobViewModel.edit(job)
                bundle.putString("name", job.name)
                bundle.putString("position", job.position)
                bundle.putLong("start", job.start)
                job.finish?.let { bundle.putLong("finish", it) }
                bundle.putString("link", job.link)

                findNavController().navigate(
                    R.id.action_navigation_profile_to_newJobFragment,
                    bundle
                )
            }

            override fun remove(job: Job) {
                jobViewModel.removeById(job.id)
            }
        }, true)

        binding.listJobs.adapter = jobsAdapter

            jobViewModel.jobData.observe(viewLifecycleOwner) {
                val listComparison = jobsAdapter.itemCount < it.size
                jobsAdapter.submitList(it){
                    if (listComparison) binding.listJobs.scrollToPosition(0)
                }
                postsAdapter.refresh()
                binding.emptyText.isVisible = it.isEmpty()
                binding.listJobs.isVisible = it.isNotEmpty()
            }

        jobViewModel.dataState.observe(viewLifecycleOwner) {
            when {
                it.error -> {
                    Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT).show()
                }
            }
            binding.progress.isVisible = it.loading
        }

        var isVisibleGroupFab = false
        binding.fab.setOnClickListener {
            if (!isVisibleGroupFab) {
                binding.fab.setImageResource(R.drawable.ic_baseline_close_24)
                binding.groupFab.visibility = View.VISIBLE
            } else {
                binding.fab.setImageResource(R.drawable.ic_baseline_add_24)
                binding.groupFab.visibility = View.GONE
            }
            isVisibleGroupFab = !isVisibleGroupFab
        }

        binding.fabAddPost.setOnClickListener {
            postViewModel.edit(Post.empty)
            findNavController().navigate(R.id.action_navigation_profile_to_newPostFragment)
        }

        binding.fabAddJob.setOnClickListener {
            jobViewModel.edit(Job.empty)
            findNavController().navigate(R.id.action_navigation_profile_to_newJobFragment)
        }

        binding.fabAddEvent.setOnClickListener{
            eventViewModel.edit(Event.empty)
            findNavController().navigate(R.id.action_navigation_my_profile_to_newEventFragment)
        }

        binding.buttonLogout.setOnClickListener {
            appAuth.removeAuth()
            findNavController().navigate(R.id.action_navigation_my_profile_to_navigation_sign_in)
        }

        return binding.root
    }
}

