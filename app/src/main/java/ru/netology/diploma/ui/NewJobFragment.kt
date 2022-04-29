package ru.netology.diploma.ui

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import ru.netology.diploma.R
import ru.netology.diploma.databinding.FragmentNewJobBinding
import ru.netology.diploma.databinding.FragmentNewPostBinding
import ru.netology.diploma.utils.Utils
import ru.netology.diploma.utils.Utils.formatDate
import ru.netology.diploma.viewmodel.JobViewModel
import ru.netology.diploma.viewmodel.PostViewModel
import java.time.Instant
import java.util.*


class NewJobFragment : Fragment() {

    private var fragmentBinding: FragmentNewJobBinding? = null
    private val viewModel: JobViewModel by activityViewModels() //TODO надо ли менять?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_new_object, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                fragmentBinding?.let {
                    viewModel.changeData(
                        start = it.start.text.toString().toLong(),
                        finish = it.finish.text.toString().toLong(),
                        company = it.companyEdit.text.toString(),
                        website = it.linkEdit.text.toString(),
                        position = it.positionEdit.text.toString()
                    )
                    viewModel.save()
                    Utils.hideKeyboard(requireView())
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewJobBinding.inflate(inflater, container, false)
        fragmentBinding = binding

        val name = arguments?.getString("name")
        val position = arguments?.getString("position")
        val start = formatDate(arguments?.getLong("start"))

        val finish =formatDate(arguments?.getLong("finish"))
        val link = arguments?.getString("link")

        with(binding) {
            startEdit.setText(start)
            finishEdit.setText(finish)
            companyEdit.setText(name)
            linkEdit.setText(link)
            positionEdit.setText(position)
        }


        viewModel.jobCreated.observe(viewLifecycleOwner) {
            viewModel.loadJobs()
            findNavController().navigateUp()
        }

        return binding.root


    }
}