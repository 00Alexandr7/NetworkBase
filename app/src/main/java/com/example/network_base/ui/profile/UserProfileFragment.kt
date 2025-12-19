package com.example.network_base.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.network_base.R
import com.example.network_base.NetworkBaseApplication
import com.example.network_base.data.model.UserRole
import com.example.network_base.data.repository.UserRepository
import com.example.network_base.databinding.FragmentUserProfileBinding
import kotlinx.coroutines.launch

/**
 * Фрагмент для отображения профиля пользователя
 */
class UserProfileFragment : Fragment() {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as NetworkBaseApplication
        userRepository = UserRepository(app.database.userDao(), app.database.achievementDao())

        // Настройка toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Загрузка данных пользователя
        loadUserData()
    }

    private fun loadUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                user?.let {
                    binding.userName.text = it.name
                    binding.userEmail.text = it.email ?: ""
                    binding.userRole.text = when (it.role) {
                        UserRole.GUEST -> "Гость"
                        UserRole.USER -> "Пользователь"
                        UserRole.ADMIN -> "Администратор"
                    }
                    binding.userXp.text = "Опыт: ${it.xp} XP"
                    binding.userLevel.text = "Уровень: ${it.getLevel()}"
                }
            } catch (e: Exception) {
                // Показать ошибку
            }
        }
    }
}
