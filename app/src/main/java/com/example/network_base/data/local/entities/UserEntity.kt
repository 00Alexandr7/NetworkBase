package com.example.network_base.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.network_base.data.model.UserProfile

/**
 * Entity для профиля пользователя
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val xp: Int,
    val currentModuleId: String?,
    val createdAt: Long
) {
    fun toUserProfile(): UserProfile {
        return UserProfile(
            id = id,
            name = name,
            xp = xp,
            currentModuleId = currentModuleId,
            createdAt = createdAt
        )
    }
    
    companion object {
        fun fromUserProfile(profile: UserProfile): UserEntity {
            return UserEntity(
                id = profile.id,
                name = profile.name,
                xp = profile.xp,
                currentModuleId = profile.currentModuleId,
                createdAt = profile.createdAt
            )
        }
    }
}

