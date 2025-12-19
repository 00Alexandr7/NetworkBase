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
import com.example.network_base.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
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
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initValidationMask()
        binding.buttonRegister.setOnClickListener { handleRegister() }
        binding.textToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collectLatest { state ->
                binding.buttonRegister.isEnabled = !state.loading
                if (state.error != null) {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun handleRegister() {
        val name = binding.inputName.text?.toString().orEmpty()
        val email = binding.inputEmail.text?.toString().orEmpty()
        val password = binding.inputPassword.text?.toString().orEmpty()
        val confirm = binding.inputPasswordConfirm.text?.toString().orEmpty()
        val transfer = false

        val errorMessage = when {
            name.length !in 2..30 -> getString(R.string.validation_name_length)
            !EMAIL_REGEX.toRegex().matches(email) -> getString(R.string.validation_email_format)
            password.length < 6 || !PASSWORD_REGEX.toRegex().matches(password) -> getString(R.string.validation_password_rules)
            password != confirm -> getString(R.string.validation_password_mismatch)
            else -> null
        }
        if (errorMessage != null) {
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.register(name, email, password, transfer) { success ->
            if (success) navigateToMain()
        }
    }

    private fun navigateToMain() {
        findNavController().navigate(R.id.action_global_courseListFragment)
    }

    private fun initValidationMask() {
        binding.inputEmail.doAfterTextChanged { text ->
            val value = text?.toString().orEmpty()
            if (!value.contains("@") || !value.contains(".")) {
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
        private const val PASSWORD_REGEX = "^[A-Za-z0-9]{6,}$"
    }
}


