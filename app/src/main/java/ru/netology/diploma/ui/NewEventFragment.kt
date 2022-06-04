package ru.netology.diploma.ui

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import ru.netology.diploma.R
import ru.netology.diploma.databinding.FragmentNewEventBinding
import ru.netology.diploma.dto.Coordinates
import ru.netology.diploma.enumeration.EventType
import ru.netology.diploma.extensions.afterTextChanged
import ru.netology.diploma.utils.Utils
import ru.netology.diploma.utils.Utils.formatToDate
import ru.netology.diploma.utils.Utils.formatToInstant
import ru.netology.diploma.utils.Utils.listToString
import ru.netology.diploma.viewmodel.EventViewModel
import ru.netology.diploma.viewmodel.UserViewModel


class NewEventFragment : Fragment() {

    private var fragmentBinding: FragmentNewEventBinding? = null
    private val eventViewModel: EventViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private var format: EventType = EventType.ONLINE

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

                    val date = it.dateEdit.text.toString()
                    val time = it.timeEdit.text.toString()
                    val link = it.linkEdit.text.toString()
                    val coordText = it.coordEdit.text.toString()
                    val content = it.edit.text.toString()
                    eventViewModel.requireData(date, time, link, coordText, content, format)

                    val eventState = eventViewModel.eventFormState.value

                    if (eventState != null) {
                        if (eventState.emptyDateError != null) it.dateEdit.error =
                            getString(eventState.emptyDateError)


                        if (eventState.emptyTimeError != null) it.timeEdit.error =
                            getString(eventState.emptyTimeError)

                        if (eventState.emptyLinkError != null) it.linkEdit.error =
                            getString(eventState.emptyLinkError)

                        if (eventState.emptyCoordError != null) it.coordEdit.error =
                            getString(eventState.emptyCoordError)

                        if (eventState.emptyContentError != null) it.edit.error =
                            getString(eventState.emptyContentError)

                        if (eventState.isDataNotBlank) {
                            val dateTime =
                                formatToInstant("${it.dateEdit.text} ${it.timeEdit.text}")
                            val coordLatLong = coordText.split(" ")

                            eventViewModel.changeContent(
                                dateTime = dateTime,
                                format = format,
                                link = link,
                                coord = if (coordLatLong.isNullOrEmpty()) Coordinates(
                                    coordLatLong[0].toDouble(),
                                    coordLatLong[1].toDouble()
                                ) else null,
                                content = content,
                            )
                            eventViewModel.save()
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
        val binding = FragmentNewEventBinding.inflate(inflater, container, false)
        fragmentBinding = binding

        val content = arguments?.getString("content")
        val dateTime = arguments?.getString("dateTime")?.let { formatToDate(it) }?.split(" ")
        val eventType = arguments?.getString("format")
        val link = arguments?.getString("link")
        val attachment = arguments?.getString("attachment")

        binding.edit.setText(content)
        binding.dateEdit.setText(dateTime?.get(0))
        binding.timeEdit.setText(dateTime?.get(1))
        binding.linkEdit.setText(link)

        //TODO сделать проверку на тип вложения
        if (attachment != null) {
            eventViewModel.changeFile(attachment.toUri())
            Glide.with(binding.photo)
                .load(attachment)
                .timeout(10_000)
                .into(binding.photo)
        }

        binding.edit.requestFocus()


        eventViewModel.edited.observe(viewLifecycleOwner) {
            val nameSpeakers = mutableListOf<String>()

            it.speakerIds.map { id ->
                userViewModel.data.value?.users?.map { user -> //TODO корректна ли такая запись?
                    if (id == user.id) nameSpeakers.add(user.name)
                }
            }

            val nameSpeakersString = listToString(nameSpeakers)

            binding.speakersEdit.setText(nameSpeakersString)
        }


        this.context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.event_type,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinner.adapter = adapter
                if (eventType == "OFFLINE") binding.spinner.setSelection(1)
            }
        }

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                itemSelected: View?, selectedItemPosition: Int, selectedId: Long
            ) {
                when (selectedItemPosition) {
                    0 -> {
                        binding.tableRowLink.visibility = View.VISIBLE
                        binding.tableRowCoord.visibility = View.GONE
                        format = EventType.ONLINE
                    }
                    1 -> {
                        binding.tableRowLink.visibility = View.GONE
                        binding.tableRowCoord.visibility = View.VISIBLE
                        format = EventType.OFFLINE
                    }
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        eventViewModel.changeFile(uri)

                    }
                }
            }

        binding.attach.setOnClickListener {
            val view = this
            PopupMenu(it.context, it).apply {
                inflate(R.menu.options_attach)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.gallery -> {
                            ImagePicker.with(view)
                                .crop()
                                .compress(2048)
                                .provider(ImageProvider.GALLERY)
                                .createIntent(pickPhotoLauncher::launch)
                            true
                        }
                        R.id.camera -> {
                            ImagePicker.with(view)
                                .crop()
                                .compress(2048)
                                .provider(ImageProvider.CAMERA)
                                .createIntent(pickPhotoLauncher::launch)
                            true
                        }
                        R.id.audio -> {
                            //TODO
                            true
                        }
                        else -> false
                    }
                }
            }.show()
        }
        binding.removeFile.setOnClickListener {
            eventViewModel.changeFile(null)
        }


        eventViewModel.eventCreated.observe(viewLifecycleOwner) {
            eventViewModel.loadEvents()
            findNavController().navigateUp()
        }

        eventViewModel.file.observe(viewLifecycleOwner) {
            if (it.uri == null) {
                binding.photoContainer.visibility = View.GONE
                return@observe
            }
            binding.photoContainer.visibility = View.VISIBLE
            binding.photo.setImageURI(it.uri)

        }

        //TODO сделать для аудио

        binding.dateEdit.setOnClickListener {
            binding.dateEdit.error = null
            context?.let { context -> Utils.showDateDialog(binding.dateEdit, context) }
        }

        binding.timeEdit.setOnClickListener {
            binding.timeEdit.error = null
            context?.let { context -> Utils.showTimeDialog(binding.timeEdit, context) }
        }

        binding.speakersEdit.setOnClickListener {

            findNavController().navigate(
                R.id.action_newEventFragment_to_speakersFragment
            )
        }

        binding.linkEdit.afterTextChanged {
            eventViewModel.isLinkValid(it)
        }

        eventViewModel.eventFormState.observe(viewLifecycleOwner, Observer {
            val eventState = it ?: return@Observer

            if (eventState.linkError != null) {
                binding.linkEdit.error = getString(eventState.linkError)
            }

        })

        //TODO сделать для editLocation ClickListener


        return binding.root


    }

}
