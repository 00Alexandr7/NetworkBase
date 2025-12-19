package com.example.network_base

import android.app.Application
import android.util.Log
import com.example.network_base.data.local.AppDatabase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

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
        initFirebase()
    }

    companion object {
        private lateinit var instance: NetworkBaseApplication

        fun getInstance(): NetworkBaseApplication = instance
    }

    private fun initFirebase() {
        runCatching {
            // Проверяем, не инициализирован ли уже Firebase
            if (FirebaseApp.getApps(this@NetworkBaseApplication).isEmpty()) {
                FirebaseApp.initializeApp(this@NetworkBaseApplication)
            }

            // Настраиваем Firestore с поддержкой оффлайн-работы
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

            FirebaseFirestore.getInstance().apply {
                firestoreSettings = settings
            }
        }.onFailure { e ->
            // Логируем возможные ошибки для отладки
            Log.e("FirebaseInit", "Ошибка при инициализации Firebase", e)
        }
    }
}