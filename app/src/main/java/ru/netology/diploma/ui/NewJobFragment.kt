package ru.netology.diploma.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.diploma.R
import ru.netology.diploma.databinding.FragmentNewJobBinding
import ru.netology.diploma.extensions.afterTextChanged
import ru.netology.diploma.utils.Utils
import ru.netology.diploma.utils.Utils.dateToEpochSec
import ru.netology.diploma.utils.Utils.formatDate
import ru.netology.diploma.utils.Utils.showDateDialog
import ru.netology.diploma.viewmodel.JobViewModel

@AndroidEntryPoint
class NewJobFragment : Fragment() {

    private var fragmentBinding: FragmentNewJobBinding? = null
    private val viewModel: JobViewModel by activityViewModels()

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
                    val start = it.startEdit.text.toString()
                    val position = it.positionEdit.text.toString()
                    val company = it.companyEdit.text.toString()

                    viewModel.requireData(start, position, company)

                    val state = viewModel.dataState.value

                     if (state != null) {
                        if (state.emptyToDate != null) {
                            it.startEdit.error =
                                getString(state.emptyToDate)

                        }

                        if (state.emptyPositionError != null) {
                            it.positionEdit.error =
                                getString(state.emptyPositionError)
                        }

                        if (state.emptyCompanyError != null) {
                            it.companyEdit.error =
                                getString(state.emptyCompanyError)
                        }

                        if (state.isDataNotBlank) {
                            dateToEpochSec(start)?.let { startLong ->
                                viewModel.changeData(
                                    start = startLong,
                                    finish = dateToEpochSec(it.finishEdit.text.toString()),
                                    company = company,
                                    website = it.linkEdit.text.toString(),
                                    position = position
                                )
                            }
                            viewModel.save()
                            Utils.hideKeyboard(requireView())
                        } else return false
                    }
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
            findNavController().navigateUp()
        }

        binding.startEdit.setOnClickListener {
            binding.startEdit.error = null
            context?.let { context -> showDateDialog(binding.startEdit, context) }
        }

        binding.finishEdit.setOnClickListener {
            context?.let { context -> showDateDialog(binding.finishEdit, context) }
        }

        binding.linkEdit.afterTextChanged {
            viewModel.isLinkValid(it)
        }

        viewModel.dataState.observe(viewLifecycleOwner, Observer {
            val state = it ?: return@Observer

            if (state.linkError != null) {
                binding.linkEdit.error = getString(state.linkError)
            }

        })

        return binding.root

    }
}


