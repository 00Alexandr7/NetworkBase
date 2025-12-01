package com.example.network_base.data.repository

import com.example.network_base.data.local.dao.SavedTopologyDao
import com.example.network_base.data.local.entities.SavedTopologyEntity
import com.example.network_base.data.model.NetworkTopology
import com.example.network_base.data.model.SavedTopology
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Репозиторий для работы с сохранёнными топологиями
 */
class TopologyRepository(
    private val savedTopologyDao: SavedTopologyDao,
    private val gson: Gson = Gson()
) {
    
    /**
     * Получить все сохранённые топологии
     */
    suspend fun getAllSavedTopologies(): List<SavedTopology> {
        return savedTopologyDao.getAllTopologies().map { it.toSavedTopology() }
    }
    
    /**
     * Получить все сохранённые топологии как Flow
     */
    fun getAllSavedTopologiesFlow(): Flow<List<SavedTopology>> {
        return savedTopologyDao.getAllTopologiesFlow().map { list ->
            list.map { it.toSavedTopology() }
        }
    }
    
    /**
     * Получить топологию по ID
     */
    suspend fun getTopologyById(id: String): NetworkTopology? {
        val saved = savedTopologyDao.getTopologyById(id) ?: return null
        return deserializeTopology(saved.topologyJson)
    }
    
    /**
     * Получить топологию по ID задания
     */
    suspend fun getTopologyByTaskId(taskId: String): NetworkTopology? {
        val saved = savedTopologyDao.getTopologyByTaskId(taskId) ?: return null
        return deserializeTopology(saved.topologyJson)
    }
    
    /**
     * Сохранить топологию
     */
    suspend fun saveTopology(
        topology: NetworkTopology, 
        name: String = topology.name,
        taskId: String? = null
    ): String {
        val id = UUID.randomUUID().toString()
        val json = serializeTopology(topology)
        val now = System.currentTimeMillis()
        
        val entity = SavedTopologyEntity(
            id = id,
            name = name,
            topologyJson = json,
            createdAt = now,
            updatedAt = now,
            taskId = taskId
        )
        
        savedTopologyDao.insertTopology(entity)
        return id
    }
    
    /**
     * Обновить существующую топологию
     */
    suspend fun updateTopology(id: String, topology: NetworkTopology, name: String? = null) {
        val existing = savedTopologyDao.getTopologyById(id) ?: return
        
        val updated = SavedTopologyEntity(
            id = id,
            name = name ?: existing.name,
            topologyJson = serializeTopology(topology),
            createdAt = existing.createdAt,
            updatedAt = System.currentTimeMillis(),
            taskId = existing.taskId
        )
        
        savedTopologyDao.updateTopology(updated)
    }
    
    /**
     * Сохранить или обновить топологию для задания
     */
    suspend fun saveTopologyForTask(taskId: String, topology: NetworkTopology): String {
        val existing = savedTopologyDao.getTopologyByTaskId(taskId)
        
        return if (existing != null) {
            updateTopology(existing.id, topology)
            existing.id
        } else {
            saveTopology(topology, topology.name, taskId)
        }
    }
    
    /**
     * Удалить топологию
     */
    suspend fun deleteTopology(id: String) {
        savedTopologyDao.deleteTopologyById(id)
    }
    
    /**
     * Получить количество сохранённых топологий
     */
    suspend fun getTopologyCount(): Int {
        return savedTopologyDao.getTopologyCount()
    }
    
    /**
     * Сбросить все сохранённые топологии
     */
    suspend fun deleteAllTopologies() {
        savedTopologyDao.deleteAll()
    }
    
    /**
     * Сериализовать топологию в JSON
     */
    private fun serializeTopology(topology: NetworkTopology): String {
        return gson.toJson(topology)
    }
    
    /**
     * Десериализовать топологию из JSON
     */
    private fun deserializeTopology(json: String): NetworkTopology? {
        return try {
            gson.fromJson(json, NetworkTopology::class.java)
        } catch (e: Exception) {
            null
        }
    }
}

