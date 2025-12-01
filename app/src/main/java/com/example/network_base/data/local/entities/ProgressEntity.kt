package com.example.network_base.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.network_base.data.model.ModuleProgress
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Entity для прогресса по модулю
 */
@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey
    val moduleId: String,
    val lessonsCompletedJson: String, // JSON массив ID уроков
    val taskCompleted: Boolean,
    val taskScore: Int,
    val taskAttempts: Int,
    val bestScore: Int
) {
    fun toModuleProgress(): ModuleProgress {
        val lessonsSet = try {
            val type = object : TypeToken<Set<String>>() {}.type
            Gson().fromJson<Set<String>>(lessonsCompletedJson, type) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
        
        return ModuleProgress(
            moduleId = moduleId,
            lessonsCompleted = lessonsSet.toMutableSet(),
            taskCompleted = taskCompleted,
            taskScore = taskScore,
            taskAttempts = taskAttempts,
            bestScore = bestScore
        )
    }
    
    companion object {
        fun fromModuleProgress(progress: ModuleProgress): ProgressEntity {
            return ProgressEntity(
                moduleId = progress.moduleId,
                lessonsCompletedJson = Gson().toJson(progress.lessonsCompleted),
                taskCompleted = progress.taskCompleted,
                taskScore = progress.taskScore,
                taskAttempts = progress.taskAttempts,
                bestScore = progress.bestScore
            )
        }
    }
}

