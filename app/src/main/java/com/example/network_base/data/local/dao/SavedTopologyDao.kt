package com.example.network_base.data.local.dao

import androidx.room.*
import com.example.network_base.data.local.entities.SavedTopologyEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с сохранёнными топологиями
 */
@Dao
interface SavedTopologyDao {
    
    @Query("SELECT * FROM saved_topologies ORDER BY updatedAt DESC")
    suspend fun getAllTopologies(): List<SavedTopologyEntity>
    
    @Query("SELECT * FROM saved_topologies ORDER BY updatedAt DESC")
    fun getAllTopologiesFlow(): Flow<List<SavedTopologyEntity>>
    
    @Query("SELECT * FROM saved_topologies WHERE id = :id")
    suspend fun getTopologyById(id: String): SavedTopologyEntity?
    
    @Query("SELECT * FROM saved_topologies WHERE taskId = :taskId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getTopologyByTaskId(taskId: String): SavedTopologyEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopology(topology: SavedTopologyEntity)
    
    @Update
    suspend fun updateTopology(topology: SavedTopologyEntity)
    
    @Delete
    suspend fun deleteTopology(topology: SavedTopologyEntity)
    
    @Query("DELETE FROM saved_topologies WHERE id = :id")
    suspend fun deleteTopologyById(id: String)
    
    @Query("DELETE FROM saved_topologies")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM saved_topologies")
    suspend fun getTopologyCount(): Int
}

