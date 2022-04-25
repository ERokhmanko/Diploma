package ru.netology.diploma.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.diploma.adapter.PostLoadStateAdapter
import ru.netology.diploma.adapter.UserCallback
import ru.netology.diploma.adapter.UsersAdapter
import ru.netology.diploma.databinding.FragmentUsersBinding
import ru.netology.diploma.dto.User
import ru.netology.diploma.viewmodel.PostViewModel
import ru.netology.diploma.viewmodel.UserViewModel

@AndroidEntryPoint
class UsersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentUsersBinding.inflate(inflater, container, false)

        val viewModel: UserViewModel by viewModels()

        val adapter = UsersAdapter(object : UserCallback {
            override fun onUser(user: User) {
                TODO("Not yet implemented")
            }

        })

        with(binding){
            listUsers.adapter = adapter


            viewModel.data.observe(viewLifecycleOwner) { model ->  //TODO верхний или этот?
                val listComparison = adapter.itemCount < model.users.size
                adapter.submitList(model.users) {
                    if (listComparison) listUsers.scrollToPosition(0)
                }
                emptyTextUser.isVisible = model.empty
            }

            viewModel.dataState.observe(viewLifecycleOwner) { state ->
                progress.isVisible = state.loading
                swiperefresh.isRefreshing = state.refreshing

            }
            return root
        }
    }
}
