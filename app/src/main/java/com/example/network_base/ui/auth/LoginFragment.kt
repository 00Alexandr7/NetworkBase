package com.example.network_base.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.network_base.NetworkBaseApplication
import com.example.network_base.R
import com.example.network_base.data.repository.AuthRepository
import com.example.network_base.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels {
        val app = requireActivity().application as NetworkBaseApplication
        val db = app.database
        AuthViewModel.Factory(
            AuthRepository(
                FirebaseAuth.getInstance(),
                FirebaseFirestore.getInstance(),
                db.userDao(),
                db.progressDao(),
                db.achievementDao()
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initValidationMask()
        binding.buttonLogin.setOnClickListener { handleLogin() }
        binding.textToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                binding.buttonLogin.isEnabled = !state.loading
                if (state.error != null) {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun handleLogin() {
        val email = binding.inputEmail.text?.toString().orEmpty()
        val password = binding.inputPassword.text?.toString().orEmpty()
        val transfer = false

        val errorMessage = when {
            email.isBlank() -> getString(R.string.validation_required)
            !EMAIL_REGEX.toRegex().matches(email) -> getString(R.string.validation_email_format)
            password.length < 6 -> getString(R.string.validation_password_rules)
            else -> null
        }
        if (errorMessage != null) {
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.login(email, password, transfer) { success ->
            if (success) {
                // Проверяем роль пользователя после успешного входа
                viewModel.checkUserRole { isAdmin ->
                    if (isAdmin) {
                        navigateToAdminPanel()
                    } else {
                        navigateToMain()
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        findNavController().navigate(R.id.action_global_courseListFragment)
    }

    private fun navigateToAdminPanel() {
        findNavController().navigate(R.id.action_loginFragment_to_adminPanelFragment)
    }



    private fun initValidationMask() {
        binding.inputEmail.doAfterTextChanged { text ->
            val value = text?.toString().orEmpty()
            if (!value.contains("@")) {
                binding.inputEmail.error = getString(R.string.auth_email_mask_hint)
            } else if (!value.contains(".")) {
                binding.inputEmail.error = getString(R.string.auth_email_mask_hint)
            } else if (EMAIL_REGEX.toRegex().matches(value)) {
                binding.inputEmail.error = null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val EMAIL_REGEX = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    }
}

