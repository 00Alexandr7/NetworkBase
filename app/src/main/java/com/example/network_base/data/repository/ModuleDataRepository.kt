package com.example.network_base.data.repository

import com.example.network_base.data.model.ModuleData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ModuleDataRepository(
    private val firestore: FirebaseFirestore
) {
    private val collection get() = firestore.collection("modules")

    suspend fun loadModules(): List<ModuleData> {
        val snapshot = collection.get().await()
        return snapshot.documents.map { doc ->
            ModuleData(
                id = doc.id,
                title = doc.getString("title").orEmpty(),
                description = doc.getString("description").orEmpty(),
                order = doc.getLong("order")?.toInt() ?: 0
            )
        }.sortedBy { it.order }
    }

    suspend fun createModule(
        title: String,
        description: String,
        order: Int
    ): String {
        val id = UUID.randomUUID().toString()
        val data = mapOf(
            "title" to title,
            "description" to description,
            "order" to order
        )
        collection.document(id).set(data).await()
        return id
    }

    suspend fun updateModule(module: ModuleData) {
        val data = mapOf(
            "title" to module.title,
            "description" to module.description,
            "order" to module.order
        )
        collection.document(module.id).set(data).await()
    }

    suspend fun deleteModule(moduleId: String) {
        collection.document(moduleId).delete().await()
    }
}
