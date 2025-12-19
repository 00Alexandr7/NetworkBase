package com.example.network_base.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.network_base.R
import com.example.network_base.databinding.FragmentAuthChoiceBinding
import com.google.firebase.auth.FirebaseAuth

class AuthChoiceFragment : Fragment() {

    private var _binding: FragmentAuthChoiceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthChoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (FirebaseAuth.getInstance().currentUser != null) {
            navigateToMain()
            return
        }

        binding.buttonLogin.setOnClickListener {
            findNavController().navigate(R.id.action_authChoiceFragment_to_loginFragment)
        }
        binding.buttonRegister.setOnClickListener {
            findNavController().navigate(R.id.action_authChoiceFragment_to_registerFragment)
        }
        binding.buttonGuest.setOnClickListener {
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        findNavController().navigate(
            R.id.action_global_courseListFragment
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


