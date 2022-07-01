package ru.netology.diploma.ui

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.diploma.R
import ru.netology.diploma.auth.AppAuth
import ru.netology.diploma.databinding.FragmentSignUpBinding
import ru.netology.diploma.extensions.afterTextChanged
import ru.netology.diploma.utils.Utils
import ru.netology.diploma.viewmodel.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentSignUpBinding.inflate(inflater, container, false)

        val viewModel: AuthViewModel by viewModels()

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
                        viewModel.changePhoto(uri)
                    }
                }
            }

        viewModel.data.observe(viewLifecycleOwner) { authState ->
            authState.token?.let { token ->
                appAuth.setAuth(authState.id, token)
            }
            findNavController().navigate(R.id.action_navigation_signUp_to_navigation_sign_in)
        }

        with(binding) {

            avatar.setOnClickListener {
                ImagePicker.with(this@SignUpFragment)
//                    .crop()
                    .cropSquare()
                    .compress(2048)
                    .provider(ImageProvider.GALLERY)
                    .createIntent(pickPhotoLauncher::launch)
            }


            viewModel.photo.observe(viewLifecycleOwner) {
                if (it.uri == null) {
                    return@observe
                }
                avatar.setImageURI(it.uri)
            }

            password.apply {
                afterTextChanged {
                    viewModel.loginDataChanged(
                        password.text.toString()
                    )
                }


                signUp.setOnClickListener {
                    Utils.hideKeyboard(requireView())
                    loading.visibility = View.VISIBLE
                    viewModel.registrationUser(
                        login.text.toString(),
                        password.text.toString(),
                        name.text.toString(),
                    )
                }
            }

            viewModel.loginFormState.observe(viewLifecycleOwner) { state ->

                binding.signUp.isEnabled = state.isDataValid

                if (state.passwordError != null) {
                    binding.password.error = getString(state.passwordError)
                }

                if (state.errorRegistration)
                    Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.retry_loading) {
                            viewModel.registrationUser(
                                login.text.toString(),
                                password.text.toString(),
                                name.text.toString(),
                            )
                        }
                        .show()
                loading.visibility = View.GONE
            }

        }

        return binding.root

    }
}