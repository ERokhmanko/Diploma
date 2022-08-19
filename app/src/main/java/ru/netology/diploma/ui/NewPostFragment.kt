package ru.netology.diploma.ui

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.diploma.R
import ru.netology.diploma.databinding.FragmentNewPostBinding
import ru.netology.diploma.dto.Coordinates
import ru.netology.diploma.enumeration.AttachmentType
import ru.netology.diploma.utils.Utils
import ru.netology.diploma.viewmodel.PostViewModel

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    private var fragmentBinding: FragmentNewPostBinding? = null
    private val viewModel: PostViewModel by activityViewModels()
    private var shared: SharedPreferences? = null
    var type: AttachmentType? = null
    var lat: Double? = null
    var lng: Double? = null

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
                    viewModel.changeContent(
                        it.edit.text.toString(),
                        coord = if (lat != null && lng != null) Coordinates(
                            lat!!,
                            lng!!
                        ) else null
                    )

                    viewModel.save()
                    Utils.hideKeyboard(requireView())
                    shared?.edit()?.clear()?.commit()

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
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)
        fragmentBinding = binding
        shared = activity?.getSharedPreferences("draft", Context.MODE_PRIVATE)
        val keyShared = "content"

        val content = arguments?.getString("content") ?: shared?.getString(keyShared, null)
        val attachment = arguments?.getString("attachment")
        val attachmentType = arguments?.getString("attachmentType")

        binding.edit.setText(content)
        binding.edit.requestFocus()

        if (attachment != null) {
            when (attachmentType) {
                "IMAGE" -> {
                    viewModel.changeFile(attachment.toUri(), AttachmentType.IMAGE)
                    Utils.uploadingMedia(binding.media, attachment)
                }
                "AUDIO" -> {
                    viewModel.changeFile(attachment.toUri(), AttachmentType.AUDIO)
                }
                "VIDEO" -> {
                    viewModel.changeFile(attachment.toUri(), AttachmentType.VIDEO)
                    Utils.uploadingMedia(binding.media, attachment)
                }
            }
        }


        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                when (activityResult.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(activityResult.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    Activity.RESULT_OK -> {
                        val uri: Uri? = activityResult.data?.data
                        viewModel.changeFile(uri, type)
                    }
                }
            }

        val pickMediaLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    viewModel.changeFile(it, type)
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

                            type = AttachmentType.IMAGE
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
            viewModel.changeFile(null, null)
        }
        binding.removeAudio.setOnClickListener {
            viewModel.changeFile(null, null)
        }


        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().apply {
                navigateUp()
            }
        }

        binding.editMentions.setOnClickListener {

            findNavController().navigate(
                R.id.action_newPostFragment_to_mentorsFragment
            )
        }

        viewModel.file.observe(viewLifecycleOwner) {
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
                    error("Unknown attachment type")}
            }
        }

        viewModel.edited.observe(viewLifecycleOwner) {
            lat = it.coords?.lat
            lng = it.coords?.long
        }

        binding.editLocation.setOnClickListener {


            shared?.edit {
                putString(keyShared, binding.edit.text.toString())
                apply()
            }

            val bundle = Bundle().apply {
                putString("fragment", "newPost")
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
        viewModel.changeFile(null, null)
    }

}
