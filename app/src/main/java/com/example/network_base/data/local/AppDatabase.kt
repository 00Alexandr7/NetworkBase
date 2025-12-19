package com.example.network_base.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.network_base.data.local.dao.*
import com.example.network_base.data.local.entities.*

/**
 * Главная база данных приложения
 */
@Database(
    entities = [
        UserEntity::class,
        ProgressEntity::class,
        AchievementEntity::class,
        SavedTopologyEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun progressDao(): ProgressDao
    abstract fun achievementDao(): AchievementDao
    abstract fun savedTopologyDao(): SavedTopologyDao
    
    companion object {
        private const val DATABASE_NAME = "network_base_db"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

