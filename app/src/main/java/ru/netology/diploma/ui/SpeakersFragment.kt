package ru.netology.diploma.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.diploma.R
import ru.netology.diploma.adapter.CheckUsersAdapter
import ru.netology.diploma.adapter.OnUserCheckListener
import ru.netology.diploma.databinding.FragmentCheckUsersBinding
import ru.netology.diploma.dto.User
import ru.netology.diploma.viewmodel.EventViewModel
import ru.netology.diploma.viewmodel.UserViewModel


@AndroidEntryPoint
class SpeakersFragment : Fragment(), OnUserCheckListener {
    private var fragmentBinding: FragmentCheckUsersBinding? = null

    private val viewModel: EventViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.title_speakers)
        val binding = FragmentCheckUsersBinding.inflate(inflater, container, false)
        fragmentBinding = binding

        val adapter = CheckUsersAdapter(this)
        binding.usersList.adapter = adapter


        userViewModel.data.observe(viewLifecycleOwner) {
            adapter.submitList(it.users)
        }

        viewModel.edited.value?.speakerIds?.map { id ->
            onCheckUser(id)
        }
       

        return binding.root
    }

    override fun onCheckUser(id: Long) {
        viewModel.checkSpeaker(id)
    }

    override fun isCheckboxVisible(user: User): Boolean {
        return true
    }

    override fun isCheckboxChecked(user: User): Boolean {
        return viewModel.isCheckboxSpeaker(user.id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_new_object, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.save -> {
                fragmentBinding.let{
                    viewModel.saveSpeakers()
                    findNavController().navigateUp()
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clear()
    }

}