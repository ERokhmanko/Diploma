package ru.netology.diploma.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import ru.netology.diploma.dto.Job
import ru.netology.diploma.dto.Post
import ru.netology.diploma.utils.Utils.uploadingAvatar
import ru.netology.diploma.viewmodel.JobViewModel
import ru.netology.diploma.viewmodel.PostViewModel
import ru.netology.diploma.viewmodel.UserViewModel
import javax.inject.Inject

const val USER_ID = "USER_ID"

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    @Inject
    lateinit var appAuth: AppAuth

    private val postViewModel: PostViewModel by activityViewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val jobViewModel: JobViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        val binding = FragmentProfileBinding.inflate(inflater, container, false)

        val bundle = Bundle()

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

            override fun remove(post: Post) {
                postViewModel.removeById(post.id)
            }

            override fun edit(post: Post) {
                postViewModel.edit(post)
                bundle.putString("content", post.content)
                findNavController().navigate(R.id.action_navigation_main_to_newPostFragment, bundle)
            }

            override fun hide(post: Post) {
                postViewModel.hidePost(post)
            }

            override fun onVideo(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.attachment?.url))
                val videoIntent = Intent.createChooser(intent, getString(R.string.media_chooser))
                startActivity(videoIntent)
            }


            override fun onRepost(post: Post) {
                TODO("Not yet implemented")
            }

            override fun onAudio(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.attachment?.url))
                val audioIntent = Intent.createChooser(intent, getString(R.string.media_chooser))
                startActivity(audioIntent)
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
                findNavController().navigate(R.id.action_navigation_profile_to_newJobFragment, bundle)
            }

            override fun remove(job: Job) {
                jobViewModel.removeById(job.id)
            }
        }, true) //TODO исправить на условие, в зависимости от того чей профиль открываем



        binding.listJobs.adapter = jobsAdapter

            //TODO сделать запись если список работ пустой
        jobViewModel.data.observe(viewLifecycleOwner) {
            jobsAdapter.submitList(it.jobs)
        }

        binding.fab.setOnClickListener {
                binding.groupFab.visibility = View.VISIBLE
            }

        binding.fabAddPost.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_newPostFragment)
        }

        return binding.root
    }
}

