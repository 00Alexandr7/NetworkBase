package com.example.network_base.data.repository

import com.example.network_base.data.local.dao.AchievementDao
import com.example.network_base.data.local.dao.UserDao
import com.example.network_base.data.local.entities.AchievementEntity
import com.example.network_base.data.local.entities.UserEntity
import com.example.network_base.data.model.Achievement
import com.example.network_base.data.model.UnlockedAchievement
import com.example.network_base.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Репозиторий для работы с данными пользователя
 */
class UserRepository(
    private val userDao: UserDao,
    private val achievementDao: AchievementDao
) {
    
    /**
     * Получить профиль пользователя
     */
    suspend fun getUser(): UserProfile? {
        return userDao.getUser()?.toUserProfile()
    }
    
    /**
     * Получить профиль пользователя как Flow
     */
    fun getUserFlow(): Flow<UserProfile?> {
        return userDao.getUserFlow().map { it?.toUserProfile() }
    }
    
    /**
     * Создать или обновить профиль пользователя
     */
    suspend fun saveUser(user: UserProfile) {
        userDao.insertUser(UserEntity.fromUserProfile(user))
    }
    
    /**
     * Добавить XP пользователю
     */
    suspend fun addXp(userId: String, amount: Int) {
        userDao.addXp(userId, amount)
    }
    
    /**
     * Обновить имя пользователя
     */
    suspend fun updateName(userId: String, name: String) {
        userDao.updateName(userId, name)
    }
    
    /**
     * Обновить текущий модуль
     */
    suspend fun updateCurrentModule(userId: String, moduleId: String?) {
        userDao.updateCurrentModule(userId, moduleId)
    }
    
    /**
     * Получить или создать пользователя (для первого запуска)
     */
    suspend fun getOrCreateUser(): UserProfile {
        return getUser() ?: run {
            val newUser = UserProfile(name = "Пользователь")
            saveUser(newUser)
            newUser
        }
    }
    
    /**
     * Получить все разблокированные достижения
     */
    suspend fun getUnlockedAchievements(): List<UnlockedAchievement> {
        return achievementDao.getAllAchievements().map { it.toUnlockedAchievement() }
    }
    
    /**
     * Получить разблокированные достижения как Flow
     */
    fun getUnlockedAchievementsFlow(): Flow<List<UnlockedAchievement>> {
        return achievementDao.getAllAchievementsFlow().map { list ->
            list.map { it.toUnlockedAchievement() }
        }
    }
    
    /**
     * Проверить, разблокировано ли достижение
     */
    suspend fun isAchievementUnlocked(achievementId: String): Boolean {
        return achievementDao.isAchievementUnlocked(achievementId)
    }
    
    /**
     * Разблокировать достижение
     */
    suspend fun unlockAchievement(achievementId: String): Boolean {
        if (achievementDao.isAchievementUnlocked(achievementId)) {
            return false
        }
        
        achievementDao.insertAchievement(
            AchievementEntity(
                achievementId = achievementId,
                unlockedAt = System.currentTimeMillis()
            )
        )
        
        // Добавляем XP за достижение
        val achievement = Achievement.getAllAchievements().find { it.id == achievementId }
        achievement?.let {
            val user = getUser()
            user?.let { u -> addXp(u.id, it.xpReward) }
        }
        
        return true
    }
    
    /**
     * Получить количество разблокированных достижений
     */
    suspend fun getUnlockedAchievementsCount(): Int {
        return achievementDao.getUnlockedCount()
    }
    
    /**
     * Сбросить все данные пользователя
     */
    suspend fun resetAllData() {
        userDao.deleteAll()
        achievementDao.deleteAll()
    }
}

