package com.example.network_base.data.local.dao

import androidx.room.*
import com.example.network_base.data.local.entities.ProgressEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с прогрессом по модулям
 */
@Dao
interface ProgressDao {
    
    @Query("SELECT * FROM progress")
    suspend fun getAllProgress(): List<ProgressEntity>
    
    @Query("SELECT * FROM progress")
    fun getAllProgressFlow(): Flow<List<ProgressEntity>>
    
    @Query("SELECT * FROM progress WHERE moduleId = :moduleId")
    suspend fun getProgressByModule(moduleId: String): ProgressEntity?
    
    @Query("SELECT * FROM progress WHERE moduleId = :moduleId")
    fun getProgressByModuleFlow(moduleId: String): Flow<ProgressEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ProgressEntity)
    
    @Update
    suspend fun updateProgress(progress: ProgressEntity)
    
    @Query("UPDATE progress SET taskCompleted = 1, taskScore = :score, taskAttempts = taskAttempts + 1, bestScore = MAX(bestScore, :score) WHERE moduleId = :moduleId")
    suspend fun completeTask(moduleId: String, score: Int)
    
    @Delete
    suspend fun deleteProgress(progress: ProgressEntity)
    
    @Query("DELETE FROM progress")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM progress WHERE taskCompleted = 1")
    suspend fun getCompletedTasksCount(): Int
    
    @Query("SELECT SUM(bestScore) FROM progress")
    suspend fun getTotalScore(): Int?
}

