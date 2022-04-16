package ru.netology.diploma.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.diploma.R
import ru.netology.diploma.auth.AppAuth
import ru.netology.diploma.databinding.FragmentSignInBinding
import ru.netology.diploma.extensions.afterTextChanged
import ru.netology.diploma.utils.Utils
import ru.netology.diploma.viewmodel.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SignInFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentSignInBinding.inflate(inflater, container, false)

        val viewModel: AuthViewModel by viewModels()


        viewModel.data.observe(viewLifecycleOwner) { authState ->
            appAuth.removeAuth()
            authState.token?.let { token ->
                appAuth.setAuth(authState.id, token)
            }
            findNavController().navigate(R.id.action_navigation_sign_in_to_navigation_main)
        }

        with(binding) {
            password.apply {
                afterTextChanged {
                    viewModel.loginDataChanged(
                        password.text.toString()
                    )
                }

                setOnEditorActionListener { _, actionId, _ ->
                    when (actionId) {
                        EditorInfo.IME_ACTION_DONE ->
                            viewModel.authUser(
                                login.text.toString(),
                                password.text.toString()
                            )
                    }
                    false
                }

                signIn.setOnClickListener {
                    Utils.hideKeyboard(requireView())
                    loading.visibility = View.VISIBLE
                    viewModel.authUser(
                        login.text.toString(),
                        password.text.toString()
                    )
                }
            }

            viewModel.loginFormState.observe(viewLifecycleOwner, Observer {
                val loginState = it ?: return@Observer

                binding.signIn.isEnabled = loginState.isDataValid

                if (loginState.passwordError != null) {
                    binding.password.error = getString(loginState.passwordError)
                }

                if (loginState.errorAuth) {
                    Snackbar.make(binding.root, R.string.error_auth, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok) {
                            loading.visibility = View.GONE
                            login.text.clear()
                            login.requestFocus()
                            password.text.clear()
                        }
                        .show()
                }
            })

            transitionSignUp.setOnClickListener {
                findNavController().navigate(R.id.action_navigation_sign_in_to_navigation_signUp)
            }
        }


        return binding.root
    }
}

