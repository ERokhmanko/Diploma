package ru.netology.diploma.ui

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

    private val viewModel: PostViewModel by activityViewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val jobViewModel: JobViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        val binding = FragmentProfileBinding.inflate(inflater, container, false)

        userViewModel.user.observe(viewLifecycleOwner) {
            with(binding) {
                userName.text = it.name
                uploadingAvatar(avatarUser, it.avatar)
            }
        }

        val postsAdapter = PostsAdapter(object : PostCallback {
            override fun onLike(post: Post) {
                TODO("Not yet implemented")
            }

            override fun onShare(post: Post) {
                TODO("Not yet implemented")
            }

            override fun remove(post: Post) {
                TODO("Not yet implemented")
            }

            override fun edit(post: Post) {
                TODO("Not yet implemented")
            }

            override fun hide(post: Post) {
                TODO("Not yet implemented")
            }

            override fun onVideo(post: Post) {
                TODO("Not yet implemented")
            }

            override fun onRepost(post: Post) {
                TODO("Not yet implemented")
            }

            override fun onAudio(post: Post) {
                TODO("Not yet implemented")
            }
        })


        binding.listPosts.adapter = postsAdapter

        lifecycleScope.launchWhenCreated {
            viewModel.userWall.collectLatest {
                postsAdapter.submitData(it)
            }

        }


        val jobsAdapter = JobsAdapter(object : JobCallback {
            override fun edit(job: Job) {
                TODO("Not yet implemented")
            }

            override fun remove(job: Job) {
                TODO("Not yet implemented")
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

