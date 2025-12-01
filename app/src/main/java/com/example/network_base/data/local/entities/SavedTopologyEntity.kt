package com.example.network_base.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.network_base.data.model.SavedTopology

/**
 * Entity для сохранённой топологии
 */
@Entity(tableName = "saved_topologies")
data class SavedTopologyEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val topologyJson: String,
    val createdAt: Long,
    val updatedAt: Long,
    val taskId: String?
) {
    fun toSavedTopology(): SavedTopology {
        return SavedTopology(
            id = id,
            name = name,
            topologyJson = topologyJson,
            createdAt = createdAt,
            updatedAt = updatedAt,
            taskId = taskId
        )
    }
    
    companion object {
        fun fromSavedTopology(topology: SavedTopology): SavedTopologyEntity {
            return SavedTopologyEntity(
                id = topology.id,
                name = topology.name,
                topologyJson = topology.topologyJson,
                createdAt = topology.createdAt,
                updatedAt = topology.updatedAt,
                taskId = topology.taskId
            )
        }
    }
}

