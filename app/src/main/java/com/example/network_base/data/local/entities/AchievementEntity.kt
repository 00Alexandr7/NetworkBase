package com.example.network_base.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.network_base.data.model.UnlockedAchievement

/**
 * Entity для разблокированного достижения
 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val achievementId: String,
    val unlockedAt: Long
) {
    fun toUnlockedAchievement(): UnlockedAchievement {
        return UnlockedAchievement(
            achievementId = achievementId,
            unlockedAt = unlockedAt
        )
    }
    
    companion object {
        fun fromUnlockedAchievement(unlocked: UnlockedAchievement): AchievementEntity {
            return AchievementEntity(
                achievementId = unlocked.achievementId,
                unlockedAt = unlocked.unlockedAt
            )
        }
    }
}

