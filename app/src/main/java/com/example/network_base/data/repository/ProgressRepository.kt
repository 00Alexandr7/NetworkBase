package com.example.network_base.data.repository

import com.example.network_base.data.local.dao.ProgressDao
import com.example.network_base.data.local.entities.ProgressEntity
import com.example.network_base.data.model.ModuleProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Репозиторий для работы с прогрессом по курсу
 */
class ProgressRepository(
    private val progressDao: ProgressDao
) {
    
    /**
     * Получить весь прогресс
     */
    suspend fun getAllProgress(): List<ModuleProgress> {
        return progressDao.getAllProgress().map { it.toModuleProgress() }
    }
    
    /**
     * Получить весь прогресс как Flow
     */
    fun getAllProgressFlow(): Flow<List<ModuleProgress>> {
        return progressDao.getAllProgressFlow().map { list ->
            list.map { it.toModuleProgress() }
        }
    }
    
    /**
     * Получить прогресс по модулю
     */
    suspend fun getModuleProgress(moduleId: String): ModuleProgress {
        return progressDao.getProgressByModule(moduleId)?.toModuleProgress()
            ?: ModuleProgress(moduleId = moduleId)
    }
    
    /**
     * Получить прогресс по модулю как Flow
     */
    fun getModuleProgressFlow(moduleId: String): Flow<ModuleProgress> {
        return progressDao.getProgressByModuleFlow(moduleId).map { 
            it?.toModuleProgress() ?: ModuleProgress(moduleId = moduleId)
        }
    }
    
    /**
     * Сохранить прогресс по модулю
     */
    suspend fun saveProgress(progress: ModuleProgress) {
        progressDao.insertProgress(ProgressEntity.fromModuleProgress(progress))
    }
    
    /**
     * Отметить урок как завершённый
     */
    suspend fun completeLesson(moduleId: String, lessonId: String) {
        val progress = getModuleProgress(moduleId)
        progress.completeLesson(lessonId)
        saveProgress(progress)
    }
    
    /**
     * Отметить задание как завершённое
     */
    suspend fun completeTask(moduleId: String, score: Int) {
        val progress = getModuleProgress(moduleId)
        progress.completeTask(score)
        saveProgress(progress)
    }
    
    /**
     * Получить количество завершённых заданий
     */
    suspend fun getCompletedTasksCount(): Int {
        return progressDao.getCompletedTasksCount()
    }
    
    /**
     * Получить общий счёт
     */
    suspend fun getTotalScore(): Int {
        return progressDao.getTotalScore() ?: 0
    }
    
    /**
     * Проверить, завершён ли модуль
     */
    suspend fun isModuleCompleted(moduleId: String, totalLessons: Int): Boolean {
        val progress = getModuleProgress(moduleId)
        return progress.isCompleted(totalLessons)
    }
    
    /**
     * Получить общий прогресс по всем модулям (0.0 - 1.0)
     */
    suspend fun getOverallProgress(totalModules: Int): Float {
        if (totalModules == 0) return 0f
        val completedTasks = getCompletedTasksCount()
        return completedTasks.toFloat() / totalModules
    }
    
    /**
     * Сбросить весь прогресс
     */
    suspend fun resetAllProgress() {
        progressDao.deleteAll()
    }
}

