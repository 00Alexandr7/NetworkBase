
package com.example.network_base.data.repository

import com.example.network_base.data.local.dao.SavedTopologyDao
import com.example.network_base.data.local.entities.SavedTopologyEntity
import com.example.network_base.data.model.NetworkTopology
import com.example.network_base.data.model.SavedTopology
import com.google.gson.Gson
import com.google.gson.JsonParseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Репозиторий для работы с топологиями сети через Room
 */
class TopologyRepository(
    private val savedTopologyDao: SavedTopologyDao
) {
    private val gson = Gson()

    private val _currentTopology = MutableStateFlow(NetworkTopology(name = ""))
    val currentTopology = _currentTopology.asStateFlow()

    /**
     * Обновить текущую топологию в памяти
     */
    fun updateTopology(topology: NetworkTopology) {
        _currentTopology.value = topology
    }

    /**
     * Очистить текущую топологию
     */
    fun clearTopology() {
        _currentTopology.value = NetworkTopology()
    }

    /**
     * Сохранить текущую топологию с произвольным именем (без привязки к заданию).
     */
    suspend fun saveTopology(name: String): SavedTopology {
        val topology = _currentTopology.value
        val json = gson.toJson(topology)
        val saved = SavedTopology(
            name = name,
            topologyJson = json
        )
        savedTopologyDao.insertTopology(SavedTopologyEntity.fromSavedTopology(saved))
        return saved
    }

    /**
     * Сохранить текущую топологию с указанным именем (для песочницы и общего списка).
     */
    suspend fun saveCurrentTopology(name: String): SavedTopology {
        val topology = _currentTopology.value
        val json = gson.toJson(topology)
        val saved = SavedTopology(
            name = name,
            topologyJson = json
        )
        savedTopologyDao.insertTopology(SavedTopologyEntity.fromSavedTopology(saved))
        return saved
    }

    /**
     * Сохранить топологию, связанную с конкретным заданием.
     */
    suspend fun saveTopologyForTask(taskId: String, topology: NetworkTopology) {
        val json = gson.toJson(topology)
        val saved = SavedTopology(
            name = "Топология для задания $taskId",
            topologyJson = json,
            taskId = taskId
        )
        savedTopologyDao.insertTopology(SavedTopologyEntity.fromSavedTopology(saved))
    }

    /**
     * Загрузить сохранённую топологию по ID и обновить currentTopology
     */
    suspend fun loadTopology(savedTopologyId: String): NetworkTopology? {
        val entity = savedTopologyDao.getTopologyById(savedTopologyId) ?: return null
        val topology = gson.fromJson(entity.topologyJson, NetworkTopology::class.java)
        _currentTopology.value = topology
        return topology
    }

    /**
     * Получить все сохранённые топологии (метаданные + JSON)
     */
    suspend fun getAllSavedTopologies(): List<SavedTopology> {
        return savedTopologyDao.getAllTopologies().map { it.toSavedTopology() }
    }

    /**
     * Получить топологию по ID
     */
    suspend fun getTopologyById(topologyId: String): SavedTopology? {
        return savedTopologyDao.getTopologyById(topologyId)?.toSavedTopology()
    }

    /**
     * Получить NetworkTopology, сохранённую для конкретного задания.
     */
    suspend fun getTopologyByTaskId(taskId: String): NetworkTopology? {
        val entity = savedTopologyDao.getTopologyByTaskId(taskId) ?: return null

        return try {
            gson.fromJson(entity.topologyJson, NetworkTopology::class.java)
        } catch (e: JsonParseException) {
            savedTopologyDao.deleteTopologyById(entity.id)
            null
        } catch (e: IllegalArgumentException) {
            savedTopologyDao.deleteTopologyById(entity.id)
            null
        } catch (e: RuntimeException) {
            savedTopologyDao.deleteTopologyById(entity.id)
            null
        }
    }

    /**
     * Удалить сохранённую топологию
     */
    suspend fun deleteSavedTopology(savedTopologyId: String) {
        savedTopologyDao.deleteTopologyById(savedTopologyId)
    }
}
