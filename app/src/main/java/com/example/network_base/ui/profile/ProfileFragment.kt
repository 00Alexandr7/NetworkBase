package com.example.network_base.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.network_base.NetworkBaseApplication
import com.example.network_base.data.model.Achievement
import com.example.network_base.data.model.UnlockedAchievement
import com.example.network_base.data.repository.ProgressRepository
import com.example.network_base.data.repository.UserRepository
import com.example.network_base.databinding.FragmentProfileBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var userRepository: UserRepository
    private lateinit var progressRepository: ProgressRepository
    private lateinit var adapter: AchievementAdapter
    
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
        
        setupRecyclerView()
        loadData()
    }
    
    private fun setupRecyclerView() {
        adapter = AchievementAdapter()
        binding.recyclerAchievements.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAchievements.adapter = adapter
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
                    updateUserUI(profile.name, profile.xp, profile.getLevel(), profile.getLevelTitle(), profile.getLevelProgress(), profile.getXpForNextLevel())
                }
                updateAchievements(unlocked)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            val completedTasks = progressRepository.getCompletedTasksCount()
            binding.textCompletedTasks.text = completedTasks.toString()
        }
    }
    
    private fun updateUserUI(
        name: String,
        xp: Int,
        level: Int,
        levelTitle: String,
        levelProgress: Float,
        xpForNextLevel: Int
    ) {
        binding.textName.text = name
        binding.textLevelTitle.text = "Уровень $level • $levelTitle"
        binding.textXp.text = "$xp XP"
        binding.textXpNext.text = "${xpForNextLevel - xp} XP до след. уровня"
        binding.progressXp.progress = (levelProgress * 100).toInt()
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
}

data class AchievementWithStatus(
    val achievement: Achievement,
    val isUnlocked: Boolean,
    val unlockedAt: Long?
)

