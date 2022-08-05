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
import ru.netology.diploma.databinding.FragmentProfileBinding
import ru.netology.diploma.dto.Post
import ru.netology.diploma.utils.Utils.uploadingAvatar
import ru.netology.diploma.viewmodel.JobViewModel
import ru.netology.diploma.viewmodel.PostViewModel
import ru.netology.diploma.viewmodel.UserViewModel


@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val postViewModel: PostViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val jobViewModel: JobViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        val binding = FragmentProfileBinding.inflate(inflater, container, false)
        val bundle = Bundle()
        binding.buttonLogout.visibility = View.GONE

        userViewModel.user.observe(viewLifecycleOwner) {
            with(binding) {
                userName.text = it.name
                uploadingAvatar(avatarUser, it.avatar)
            }
        }

        val postsAdapter = PostsAdapter(object : PostCallback {
            override fun onLike(post: Post) {
                if (!post.likedByMe) postViewModel.likeById(post.id) else postViewModel.unlikeById(post.id)

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

            override fun onVideo(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.attachment?.url))
                val videoIntent = Intent.createChooser(intent, getString(R.string.media_chooser))
                startActivity(videoIntent)
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
                findNavController().navigate(R.id.action_anotherProfileFragment_to_usersBottomSheet)
            }

            override fun onLikeOwner(post: Post) {
                userViewModel.getUsersIds(post.likeOwnerIds)
                findNavController().navigate(R.id.action_anotherProfileFragment_to_usersBottomSheet)
            }

            override fun onMap(post: Post) {
                post.coords?.lat?.let { bundle.putDouble("lat", it) }
                post.coords?.long?.let { bundle.putDouble("lng", it) }
                findNavController().navigate(
                    R.id.action_anotherProfileFragment_to_mapFragment,
                    bundle
                )
            }
        })

        binding.listPosts.adapter = postsAdapter

        lifecycleScope.launchWhenCreated {
            postViewModel.userWall.collectLatest {
                postsAdapter.submitData(it)
            }

        }


        val jobsAdapter = JobsAdapter(object : JobCallback {}, false)



        binding.listJobs.adapter = jobsAdapter

        jobViewModel.jobData.observe(viewLifecycleOwner) {
            jobsAdapter.submitList(it)
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


        binding.fab.visibility = View.GONE

        return binding.root
    }
}

