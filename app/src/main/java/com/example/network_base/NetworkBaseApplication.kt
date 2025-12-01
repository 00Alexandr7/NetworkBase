package com.example.network_base

import android.app.Application
import com.example.network_base.data.local.AppDatabase

/**
 * Application class для инициализации базы данных и глобальных компонентов
 */
class NetworkBaseApplication : Application() {
    
    // Lazy initialization базы данных
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        private lateinit var instance: NetworkBaseApplication
        
        fun getInstance(): NetworkBaseApplication = instance
    }
}

