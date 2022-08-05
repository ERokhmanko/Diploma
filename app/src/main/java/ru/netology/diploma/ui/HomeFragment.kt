package ru.netology.diploma.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.diploma.R
import ru.netology.diploma.adapter.PostCallback
import ru.netology.diploma.adapter.PostLoadStateAdapter
import ru.netology.diploma.adapter.PostsAdapter
import ru.netology.diploma.databinding.FragmentHomeBinding
import ru.netology.diploma.dto.Post
import ru.netology.diploma.viewmodel.JobViewModel
import ru.netology.diploma.viewmodel.PostViewModel
import ru.netology.diploma.viewmodel.UserViewModel


@AndroidEntryPoint
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        val viewModel: PostViewModel by activityViewModels()
        val userViewModel: UserViewModel by activityViewModels()

        val bundle = Bundle()

        val adapter = PostsAdapter(object : PostCallback {
            override fun onLike(post: Post) {
                if (!post.likedByMe) viewModel.likeById(post.id) else viewModel.unlikeById(post.id)
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
                viewModel.removeById(post.id)
            }

            override fun edit(post: Post) {
                viewModel.edit(post)
                bundle.putString("content", post.content)
                bundle.putString("attachment", post.attachment?.url)
                bundle.putString("attachmentType", post.attachment?.type?.name.toString())
                findNavController().navigate(R.id.action_navigation_main_to_newPostFragment, bundle)
            }

            override fun hide(post: Post) {
                //Этот метод сделан для примера различного меню у постов и не имеет корректной реализации.
                viewModel.hidePost(post)
            }

            override fun onVideo(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.attachment?.url))
                val videoIntent = Intent.createChooser(intent, getString(R.string.media_chooser))
                startActivity(videoIntent)
            }


            override fun onRepost(post: Post) {

                viewModel.changeContent(post.content, post.coords)
                if (post.attachment != null) {
                    viewModel.attachmentRepost(post.attachment)
                }
                viewModel.save()

                val toast = Toast.makeText(
                    context,
                    R.string.repost,
                    Toast.LENGTH_LONG
                )
                toast.show()

            }

            override fun onMentors(post: Post) {
                userViewModel.getUsersIds(post.mentionIds)
                findNavController().navigate(R.id.action_navigation_main_to_usersBottomSheet)
            }

            override fun onMap(post: Post) {
                post.coords?.lat?.let { bundle.putDouble("lat", it) }
                post.coords?.long?.let { bundle.putDouble("lng", it) }
                findNavController().navigate(R.id.action_navigation_main_to_mapFragment, bundle)
            }

            override fun onAudio(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.attachment?.url))
                val audioIntent = Intent.createChooser(intent, getString(R.string.media_chooser))
                startActivity(audioIntent)

            }

            override fun onLikeOwner(post: Post) {
                userViewModel.getUsersIds(post.likeOwnerIds)
                findNavController().navigate(R.id.action_navigation_main_to_usersBottomSheet)
            }
        })

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadStateAdapter {
                adapter.retry()
            },
            footer = PostLoadStateAdapter {
                adapter.retry()
            }
        )

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swipeRefresh.isRefreshing =
                    state.refresh is LoadState.Loading ||
                            state.prepend is LoadState.Loading ||
                            state.append is LoadState.Loading
            }
        }

        binding.swipeRefresh.setOnRefreshListener(adapter::refresh)

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }
}
