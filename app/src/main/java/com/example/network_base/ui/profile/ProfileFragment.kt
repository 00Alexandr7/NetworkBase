package com.example.network_base.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavOptions
import com.example.network_base.NetworkBaseApplication
import com.example.network_base.R
import com.example.network_base.data.model.Achievement
import com.example.network_base.data.model.UnlockedAchievement
import com.example.network_base.data.model.UserRole
import com.example.network_base.data.repository.AuthRepository
import com.example.network_base.data.repository.ProgressRepository
import com.example.network_base.data.repository.UserRepository
import com.example.network_base.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var currentRole: UserRole = UserRole.GUEST
    
    private lateinit var userRepository: UserRepository
    private lateinit var progressRepository: ProgressRepository
    private lateinit var adapter: AchievementAdapter
    private lateinit var authRepository: AuthRepository

    private val pickAvatar = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            runCatching {
                requireContext().contentResolver.takePersistableUriPermission(uri, flags)
            }
            saveAvatarUri(uri)
            showAvatar(uri)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val app = requireActivity().application as NetworkBaseApplication
        userRepository = UserRepository(app.database.userDao(), app.database.achievementDao())
        progressRepository = ProgressRepository(app.database.progressDao())
        authRepository = AuthRepository(
            FirebaseAuth.getInstance(),
            FirebaseFirestore.getInstance(),
            app.database.userDao(),
            app.database.progressDao(),
            app.database.achievementDao()
        )
        
        setupRecyclerView()
        setupToolbar()
        setupAvatar()
        loadData()
    }

    private fun setupAvatar() {
        binding.imageAvatar.setOnClickListener {
            pickAvatar.launch(arrayOf("image/*"))
        }
        loadAvatar()?.let { uri ->
            showAvatar(uri)
        }
    }
    
    private fun setupRecyclerView() {
        adapter = AchievementAdapter()
        binding.recyclerAchievements.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAchievements.adapter = adapter
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_profile_admin)
        binding.toolbar.menu.findItem(R.id.action_admin_panel)?.isVisible = false
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_admin_panel) {
                if (currentRole == UserRole.ADMIN) {
                    findNavController().navigate(R.id.action_profileFragment_to_adminPanelFragment)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }
    
    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Ensure user exists
            userRepository.getOrCreateUser()
            
            // Observe user profile
            userRepository.getUserFlow().combine(
                userRepository.getUnlockedAchievementsFlow()
            ) { user, unlocked ->
                user to unlocked
            }.collectLatest { (user, unlocked) ->
                user?.let { profile ->
                    updateUserUI(
                        name = profile.name,
                        email = profile.email ?: getString(R.string.auth_guest_keep),
                        role = profile.role,
                        xp = profile.xp,
                        level = profile.getLevel(),
                        levelTitle = profile.getLevelTitle(),
                        levelProgress = profile.getLevelProgress(),
                        xpForNextLevel = profile.getXpForNextLevel()
                    )
                }
                updateAchievements(unlocked)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            val completedTasks = progressRepository.getCompletedTasksCount()
            binding.textCompletedTasks.text = completedTasks.toString()
        }

        binding.buttonLogout.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                authRepository.logout()
                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
                findNavController().navigate(R.id.authChoiceFragment, null, options)
            }
        }
    }
    
    private fun updateUserUI(
        name: String,
        email: String,
        role: UserRole,
        xp: Int,
        level: Int,
        levelTitle: String,
        levelProgress: Float,
        xpForNextLevel: Int
    ) {
        currentRole = role
        binding.textName.text = name
        binding.textEmail.text = email
        binding.textRole.text = role.name
        binding.textLevelTitle.text = "Уровень $level • $levelTitle"
        binding.textXp.text = "$xp XP"
        binding.textXpNext.text = "${xpForNextLevel - xp} XP до след. уровня"
        binding.progressXp.progress = (levelProgress * 100).toInt()
        binding.toolbar.menu.findItem(R.id.action_admin_panel)?.isVisible = role == UserRole.ADMIN
    }

    private fun showAvatar(uri: Uri) {
        binding.imageAvatar.setImageURI(uri)
    }

    private fun saveAvatarUri(uri: Uri) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_AVATAR_URI, uri.toString()).apply()
    }

    private fun loadAvatar(): Uri? {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_AVATAR_URI, null) ?: return null
        return runCatching { Uri.parse(raw) }.getOrNull()
    }
    
    private fun updateAchievements(unlocked: List<UnlockedAchievement>) {
        binding.textAchievementsCount.text = "${unlocked.size}"
        
        val allAchievements = Achievement.getAllAchievements()
        val unlockedIds = unlocked.map { it.achievementId }.toSet()
        
        val items = allAchievements.map { achievement ->
            AchievementWithStatus(
                achievement = achievement,
                isUnlocked = achievement.id in unlockedIds,
                unlockedAt = unlocked.find { it.achievementId == achievement.id }?.unlockedAt
            )
        }.sortedByDescending { it.isUnlocked }
        
        adapter.submitList(items)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        private const val PREFS_NAME = "network_base_prefs"
        private const val KEY_AVATAR_URI = "avatar_uri"
    }
}

data class AchievementWithStatus(
    val achievement: Achievement,
    val isUnlocked: Boolean,
    val unlockedAt: Long?
)

