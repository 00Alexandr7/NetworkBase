package com.example.network_base.data.local.dao

import androidx.room.*
import com.example.network_base.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с профилем пользователя
 */
@Dao
interface UserDao {
    
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUser(): UserEntity?
    
    @Query("SELECT * FROM users LIMIT 1")
    fun getUserFlow(): Flow<UserEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("UPDATE users SET xp = xp + :amount WHERE id = :userId")
    suspend fun addXp(userId: String, amount: Int)
    
    @Query("UPDATE users SET name = :name WHERE id = :userId")
    suspend fun updateName(userId: String, name: String)
    
    @Query("UPDATE users SET currentModuleId = :moduleId WHERE id = :userId")
    suspend fun updateCurrentModule(userId: String, moduleId: String?)

    @Query("UPDATE users SET role = :role WHERE id = :userId")
    suspend fun updateRole(userId: String, role: String)
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("DELETE FROM users")
    suspend fun deleteAll()
}

