package ru.netology.diploma.ui

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import ru.netology.diploma.R
import ru.netology.diploma.databinding.FragmentNewPostBinding
import ru.netology.diploma.utils.Utils
import ru.netology.diploma.viewmodel.PostViewModel


class NewPostFragment : Fragment() {

    private var fragmentBinding: FragmentNewPostBinding? = null
    private val viewModel: PostViewModel by activityViewModels()

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
                    viewModel.changeContent(it.edit.text.toString())
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
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)
        fragmentBinding = binding

        val content = arguments?.getString("content")
        val attachment = arguments?.getString("attachment")

        binding.edit.setText(content)
        binding.edit.requestFocus()

        //TODO сделать проверку на тип вложения
        if (attachment != null) {
            viewModel.changeFile(attachment.toUri())
            Glide.with(binding.photo)
                .load(attachment)
                .timeout(10_000)
                .into(binding.photo)
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
                        viewModel.changeFile(uri)

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
            viewModel.changeFile(null)
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPosts()
            findNavController().navigateUp()
        }

        binding.editMentions.setOnClickListener {

            findNavController().navigate(
                R.id.action_newPostFragment_to_mentorsFragment
            )
        }

        viewModel.file.observe(viewLifecycleOwner) {
            if (it.uri == null) {
                binding.photoContainer.visibility = View.GONE
                return@observe
            }

            binding.photoContainer.visibility = View.VISIBLE
            binding.photo.setImageURI(it.uri)
        }

        //TODO сделать для аудио


        return binding.root


    }
}