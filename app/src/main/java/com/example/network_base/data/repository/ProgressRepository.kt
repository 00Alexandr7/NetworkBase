
package com.example.network_base.data.repository

import com.example.network_base.data.local.dao.ProgressDao
import com.example.network_base.data.local.entities.ProgressEntity
import com.example.network_base.data.model.ModuleProgress
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Репозиторий для отслеживания прогресса пользователя поверх Room.
 * Используется как локальное хранилище, которое затем синхронизируется
 * с Firestore через AuthRepository.
 */
class ProgressRepository(
    private val progressDao: ProgressDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Поток прогресса по всем модулям.
     */
    fun getAllProgressFlow(): Flow<List<ModuleProgress>> {
        return progressDao.getAllProgressFlow().map { list ->
            list.map { it.toModuleProgress() }
        }
    }

    /**
     * Поток прогресса по конкретному модулю.
     */
    fun getModuleProgressFlow(moduleId: String): Flow<ModuleProgress?> {
        return progressDao.getProgressByModuleFlow(moduleId).map { entity ->
            entity?.toModuleProgress()
        }
    }

    /**
     * Получить или создать прогресс для модуля.
     */
    suspend fun getOrCreateModuleProgress(moduleId: String): ModuleProgress = withContext(ioDispatcher) {
        val existing = progressDao.getProgressByModule(moduleId)
        existing?.toModuleProgress() ?: ModuleProgress(moduleId).also { progress ->
            progressDao.insertProgress(ProgressEntity.fromModuleProgress(progress))
        }
    }

    /**
     * Отметить урок как завершённый и сохранить прогресс.
     */
    suspend fun completeLesson(moduleId: String, lessonId: String) = withContext(ioDispatcher) {
        val current = getOrCreateModuleProgress(moduleId)
        current.completeLesson(lessonId)
        progressDao.insertProgress(ProgressEntity.fromModuleProgress(current))
    }

    /**
     * Завершить задание, обновив счёт и лучшую попытку.
     */
    suspend fun completeTask(moduleId: String, score: Int) = withContext(ioDispatcher) {
        progressDao.completeTask(moduleId, score)
    }

    /**
     * Получить количество завершённых заданий.
     */
    suspend fun getCompletedTasksCount(): Int = withContext(ioDispatcher) {
        progressDao.getCompletedTasksCount()
    }

    /**
     * Удалить весь прогресс (используется при выходе/сбросе).
     */
    suspend fun clearAll() = withContext(ioDispatcher) {
        progressDao.deleteAll()
    }
}

