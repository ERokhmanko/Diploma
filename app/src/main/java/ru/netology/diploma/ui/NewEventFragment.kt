package ru.netology.diploma.ui

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.diploma.R
import ru.netology.diploma.databinding.FragmentNewEventBinding
import ru.netology.diploma.dto.Coordinates
import ru.netology.diploma.enumeration.AttachmentType
import ru.netology.diploma.enumeration.EventType
import ru.netology.diploma.extensions.afterTextChanged
import ru.netology.diploma.utils.Utils
import ru.netology.diploma.utils.Utils.formatToDate
import ru.netology.diploma.utils.Utils.formatToInstant
import ru.netology.diploma.utils.Utils.listToString
import ru.netology.diploma.viewmodel.EventViewModel
import ru.netology.diploma.viewmodel.UserViewModel

@AndroidEntryPoint
class NewEventFragment : Fragment() {

    private var fragmentBinding: FragmentNewEventBinding? = null
    private val eventViewModel: EventViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private var shared: SharedPreferences? = null
    private var format: EventType = EventType.ONLINE
    var type: AttachmentType? = null

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
                                coord = if (coordText.isNotEmpty()) Coordinates(
                                    coordLatLong[0].toDouble(),
                                    coordLatLong[1].toDouble()
                                ) else null,
                                content = content,
                            )
                            eventViewModel.save()
                            Utils.hideKeyboard(requireView())
                            shared?.edit()?.clear()?.apply()
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
        shared = activity?.getSharedPreferences("draft", Context.MODE_PRIVATE)

        val keyContent = "content"
        val keyDate = "date"
        val keyTime = "time"
        val keyLink = "link"
        val keyEventType = "format"


        val content = arguments?.getString("content") ?: shared?.getString(keyContent, null)
        val date = arguments?.getString("dateTime")?.let { formatToDate(it) }?.split(" ")?.get(0)
            ?: shared?.getString(
                keyDate, null
            )
        val time =
            arguments?.getString("dateTime")?.let { formatToDate(it) }?.split(" ")?.get(1)
                ?: shared?.getString(
                    keyTime, null
                )
        val eventType = arguments?.getString("format") ?: shared?.getString(keyEventType, null)
        val link = arguments?.getString("link") ?: shared?.getString(keyLink, null)

        val attachment = arguments?.getString("attachment")
        val attachmentType = arguments?.getString("attachmentType")

        var lat: Double? = null
        var lng: Double? = null

        binding.edit.setText(content)
        binding.dateEdit.setText(date)
        binding.timeEdit.setText(time)
        binding.linkEdit.setText(link)

        if (attachment != null) {
            when (attachmentType) {
                "IMAGE" -> {
                    eventViewModel.changeFile(attachment.toUri(), AttachmentType.IMAGE)
                    Utils.uploadingMedia(binding.media, attachment)
                }
                "AUDIO" -> {
                    eventViewModel.changeFile(attachment.toUri(), AttachmentType.AUDIO)
                }
                "VIDEO" -> {
                    eventViewModel.changeFile(attachment.toUri(), AttachmentType.VIDEO)
                    Utils.uploadingMedia(binding.media, attachment)
                }
            }
        }

        binding.edit.requestFocus()


        eventViewModel.edited.observe(viewLifecycleOwner) {event->
            val nameSpeakers = mutableListOf<String>()

            event.speakerIds.map { id ->
                userViewModel.getUserName(id)?.let { nameSpeakers.add(it) }
            }

            lat = event.coords?.lat
            lng = event.coords?.long
            val nameSpeakersString = listToString(nameSpeakers)

            binding.speakersEdit.setText(nameSpeakersString)
            if (lat != null && lng != null) binding.coordEdit.setText(
                getString(
                    R.string.coordinates,
                    lat.toString(),
                    lng.toString()
                )
            )
        }


        this.context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.event_type,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinner.adapter = adapter
                if (eventType == "OFFLINE" || eventType == "offline") binding.spinner.setSelection(1)
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
                        eventViewModel.changeFile(uri, type)

                    }
                }
            }

        val pickMediaLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    eventViewModel.changeFile(it, type)
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
                                .provider(ImageProvider.BOTH)
                                .createIntent(pickPhotoLauncher::launch)
                            true
                        }
                        R.id.video -> {
                            pickMediaLauncher.launch("video/*")
                            type = AttachmentType.VIDEO
                            true
                        }
                        R.id.audio -> {
                            pickMediaLauncher.launch("audio/*")
                            type = AttachmentType.AUDIO
                            true
                        }
                        else -> false
                    }
                }
            }.show()
        }
        binding.removeFile.setOnClickListener {
            eventViewModel.changeFile(null, null)
        }

        binding.removeAudio.setOnClickListener {
            eventViewModel.changeFile(null, null)
        }

        eventViewModel.eventCreated.observe(viewLifecycleOwner) {
            findNavController().apply {
                navigateUp()
            }
        }

        eventViewModel.file.observe(viewLifecycleOwner) {
            if (it.uri == null) {
                binding.mediaContainer.visibility = View.GONE
                binding.audioContainer.visibility = View.GONE
                return@observe
            }

            when (it.type) {
                AttachmentType.IMAGE -> {
                    binding.mediaContainer.visibility = View.VISIBLE
                    binding.media.setImageURI(it.uri)
                }
                AttachmentType.AUDIO -> {
                    binding.audioContainer.visibility = View.VISIBLE
                }
                AttachmentType.VIDEO -> {
                    binding.mediaContainer.visibility = View.VISIBLE
                    Utils.uploadingMedia(binding.media, it.uri.toString())
                }
                else -> {
                    error("Unknown attachment type")
                }
            }
        }

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

        binding.editLocation.setOnClickListener {

            shared?.edit {
                putString(keyContent, binding.edit.text.toString())
                putString(keyDate, binding.dateEdit.text.toString())
                putString(keyTime, binding.timeEdit.text.toString())
                putString(keyLink, binding.linkEdit.text.toString())
                putString(keyEventType, binding.spinner.selectedItem.toString())
                apply()
            }

            val bundle = Bundle().apply {
                putString("fragment", "newEvent")
                if (lat != null) {
                    putDouble("lat", lat!!)
                }
                if (lng != null) {
                    putDouble("lng", lng!!)
                }
            }
            findNavController().navigate(
                R.id.mapFragment, bundle
            )
        }

        return binding.root


    }

    override fun onDestroyView() {
        super.onDestroyView()
        eventViewModel.changeFile(null, null)
    }

}
