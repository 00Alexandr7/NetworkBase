package com.example.network_base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.network_base.databinding.ActivityMainBinding
import com.example.network_base.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupNavigation()
        setupEdgeToEdge()
    }

    fun toggleTheme() {
        val nextIsNight = !isNightModeEnabled()
        saveNightModeEnabled(nextIsNight)
        AppCompatDelegate.setDefaultNightMode(
            if (nextIsNight) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        recreate()
    }

    fun isNightModeEnabled(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getBoolean(KEY_NIGHT_MODE, false)
    }

    private fun applySavedTheme() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isNight = prefs.getBoolean(KEY_NIGHT_MODE, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isNight) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun saveNightModeEnabled(enabled: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NIGHT_MODE, enabled).apply()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        binding.bottomNavigation.setupWithNavController(navController)

        // По умолчанию скрываем админку, показываем только после проверки роли
        binding.bottomNavigation.menu.findItem(R.id.adminPanelFragment)?.isVisible = false
        
        // Проверка прав администратора
        lifecycleScope.launch {
            checkAdminRights()
        }
        

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.lessonFragment,
                R.id.taskFragment,
                R.id.authChoiceFragment,
                R.id.loginFragment,
                R.id.registerFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
            }
        }
    }
    
    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }
    
    private suspend fun checkAdminRights() {
        try {
            val auth = FirebaseAuth.getInstance()
            val uid = auth.currentUser?.uid
            if (uid != null) {
                val firestore = FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("users").document(uid).get().await()
                val isAdmin = UserRole.fromString(snapshot.getString("role")) == UserRole.ADMIN
                
                runOnUiThread {
                    // Показываем или скрываем пункт меню администратора
                    binding.bottomNavigation.menu.findItem(R.id.adminPanelFragment)?.isVisible = isAdmin
                }
            }
        } catch (e: Exception) {
            // В случае ошибки скрываем пункт меню
            runOnUiThread {
                binding.bottomNavigation.menu.findItem(R.id.adminPanelFragment)?.isVisible = false
            }
        }
    }

    private companion object {
        private const val PREFS_NAME = "network_base_prefs"
        private const val KEY_NIGHT_MODE = "night_mode"
    }
}
