package ru.netology.diploma.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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

        val viewModel: PostViewModel by viewModels(
//             ownerProducer = ::requireParentFragment
        )

        val adapter = PostsAdapter(object : PostCallback {
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

            override fun onImage(post: Post) {
                TODO("Not yet implemented")
            }

            override fun onPost(post: Post) {
                TODO("Not yet implemented")
            }

            override fun onRepost(post: Post) {
                TODO("Not yet implemented")
            }

            override fun onAudio(post: Post) {
                TODO("Not yet implemented")
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
        return binding.root
    }
}
