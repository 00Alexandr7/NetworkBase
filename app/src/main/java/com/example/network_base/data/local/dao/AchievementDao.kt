package com.example.network_base.data.local.dao

import androidx.room.*
import com.example.network_base.data.local.entities.AchievementEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с достижениями
 */
@Dao
interface AchievementDao {
    
    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievements(): List<AchievementEntity>
    
    @Query("SELECT * FROM achievements")
    fun getAllAchievementsFlow(): Flow<List<AchievementEntity>>
    
    @Query("SELECT * FROM achievements WHERE achievementId = :achievementId")
    suspend fun getAchievement(achievementId: String): AchievementEntity?
    
    @Query("SELECT EXISTS(SELECT 1 FROM achievements WHERE achievementId = :achievementId)")
    suspend fun isAchievementUnlocked(achievementId: String): Boolean
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievement(achievement: AchievementEntity)
    
    @Delete
    suspend fun deleteAchievement(achievement: AchievementEntity)
    
    @Query("DELETE FROM achievements")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getUnlockedCount(): Int
}

