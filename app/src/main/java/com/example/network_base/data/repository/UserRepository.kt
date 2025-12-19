
package com.example.network_base.data.repository

import com.example.network_base.data.local.dao.AchievementDao
import com.example.network_base.data.local.dao.UserDao
import com.example.network_base.data.local.entities.AchievementEntity
import com.example.network_base.data.local.entities.UserEntity
import com.example.network_base.data.model.Achievement
import com.example.network_base.data.model.UnlockedAchievement
import com.example.network_base.data.model.UserProfile
import com.example.network_base.data.model.UserRole
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Репозиторий для работы с данными пользователя и достижениями поверх Room.
 *
 * Используется как для гостя (локальный профиль без Firebase UID),
 * так и для авторизованного пользователя (профиль кэшируется из Firebase).
 */
class UserRepository(
    private val userDao: UserDao,
    private val achievementDao: AchievementDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Получить текущего пользователя.
     */
    suspend fun getCurrentUser(): UserProfile? = withContext(ioDispatcher) {
        userDao.getUser()?.toUserProfile()
    }

    /**
     * Получить или создать локального пользователя-гостя.
     * Если пользователь уже есть в БД, он возвращается как есть.
     */
    suspend fun getOrCreateUser(): UserProfile = withContext(ioDispatcher) {
        val existing = userDao.getUser()?.toUserProfile()
        if (existing != null) {
            existing
        } else {
            val guest = UserProfile(
                // id будет сгенерирован в модели
                role = UserRole.GUEST,
                xp = 0,
                currentModuleId = null
            )
            userDao.insertUser(UserEntity.fromUserProfile(guest))
            guest
        }
    }

    /**
     * Поток профиля пользователя из Room.
     */
    fun getUserFlow(): Flow<UserProfile?> {
        return userDao.getUserFlow().map { entity ->
            entity?.toUserProfile()
        }
    }

    /**
     * Установить/обновить профиль пользователя в Room.
     * Используется после синхронизации с Firebase.
     */
    suspend fun saveUser(profile: UserProfile) = withContext(ioDispatcher) {
        userDao.insertUser(UserEntity.fromUserProfile(profile))
    }

    /**
     * Добавить XP пользователю и сохранить в Room.
     * Возвращает количество полученных уровней.
     */
    suspend fun addXp(userId: String, amount: Int): Int = withContext(ioDispatcher) {
        val entity = userDao.getUser()
        if (entity == null || entity.id != userId) {
            0
        } else {
            val profile = entity.toUserProfile()
            val levelsGained = profile.addXp(amount)
            userDao.insertUser(UserEntity.fromUserProfile(profile))
            levelsGained
        }
    }

    /**
     * Обновить имя пользователя.
     */
    suspend fun updateName(userId: String, name: String) = withContext(ioDispatcher) {
        userDao.updateName(userId, name)
    }

    /**
     * Обновить текущий модуль пользователя.
     */
    suspend fun updateCurrentModule(userId: String, moduleId: String?) = withContext(ioDispatcher) {
        userDao.updateCurrentModule(userId, moduleId)
    }

    /**
     * Разблокировать достижение (если ещё не было) и сохранить в Room.
     */
    suspend fun unlockAchievement(achievementId: String): Boolean = withContext(ioDispatcher) {
        if (achievementDao.isAchievementUnlocked(achievementId)) {
            false
        } else {
            val entity = AchievementEntity(
                achievementId = achievementId,
                unlockedAt = System.currentTimeMillis()
            )
            achievementDao.insertAchievement(entity)

            // Дополнительно увеличим счётчик достижений у пользователя, если он есть
            val user = userDao.getUser()
            if (user != null) {
                val profile = user.toUserProfile()
                val newProfile = profile.cloneWithValues(
                    totalAchievements = profile.totalAchievements + 1
                )
                userDao.insertUser(UserEntity.fromUserProfile(newProfile))
            }
            true
        }
    }

    /**
     * Поток разблокированных достижений.
     */
    fun getUnlockedAchievementsFlow(): Flow<List<UnlockedAchievement>> {
        return achievementDao.getAllAchievementsFlow().map { list ->
            list.map { it.toUnlockedAchievement() }
        }
    }

    /**
     * Получить все достижения единоразово.
     */
    suspend fun getUnlockedAchievements(): List<UnlockedAchievement> = withContext(ioDispatcher) {
        achievementDao.getAllAchievements().map { it.toUnlockedAchievement() }
    }
}

