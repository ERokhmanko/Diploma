package ru.netology.diploma.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import ru.netology.diploma.viewmodel.PostViewModel

@AndroidEntryPoint
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        val viewModel: PostViewModel by activityViewModels()

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
                findNavController().navigate(R.id.action_navigation_main_to_newPostFragment, bundle)
            }

            override fun hide(post: Post) {
                viewModel.hidePost(post)
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
            adapter.loadStateFlow.collectLatest {
                binding.swipeRefresh.isRefreshing =
                    it.refresh is LoadState.Loading
            }
        }

//        lifecycleScope.launchWhenCreated {
//            adapter.loadStateFlow.collectLatest { state ->
//                binding.swipeRefresh.isRefreshing =
//                    state.refresh is LoadState.Loading ||
//                            state.prepend is LoadState.Loading ||
//                            state.append is LoadState.Loading
//            }
//        } TODO проверить какой вариант лучше

        binding.swipeRefresh.setOnRefreshListener(adapter::refresh)
        return binding.root
    }
}
