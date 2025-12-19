package com.example.network_base.data.repository

import com.example.network_base.data.model.Theory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class TheoryDataRepository(
    private val firestore: FirebaseFirestore
) {
    private val collection get() = firestore.collection("lessons")

    suspend fun loadTheories(): List<Theory> {
        val snapshot = collection.get().await()
        return snapshot.documents.map { doc ->
            Theory(
                id = doc.id,
                lessonId = doc.id,
                moduleId = doc.getString("moduleId").orEmpty(),
                title = doc.getString("title").orEmpty(),
                content = doc.getString("content").orEmpty(),
                order = doc.getLong("order")?.toInt() ?: 0
            )
        }.sortedBy { it.order }
    }

    suspend fun loadTheoriesByLesson(lessonId: String): List<Theory> {
        val doc = collection.document(lessonId).get().await()
        if (!doc.exists()) return emptyList()
        return listOf(
            Theory(
                id = doc.id,
                lessonId = doc.id,
                moduleId = doc.getString("moduleId").orEmpty(),
                title = doc.getString("title").orEmpty(),
                content = doc.getString("content").orEmpty(),
                order = doc.getLong("order")?.toInt() ?: 0
            )
        )
    }

    suspend fun createTheory(
        moduleId: String,
        title: String,
        content: String,
        order: Int
    ): String {
        val id = UUID.randomUUID().toString()
        val data = mapOf(
            "moduleId" to moduleId,
            "title" to title,
            "content" to content,
            "type" to "text",
            "order" to order,
            "isPublished" to false,
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis()
        )
        collection.document(id).set(data).await()
        return id
    }

    suspend fun updateTheory(theory: Theory) {
        val data = mapOf(
            "moduleId" to theory.moduleId,
            "title" to theory.title,
            "content" to theory.content,
            "order" to theory.order,
            "updatedAt" to System.currentTimeMillis()
        )
        collection.document(theory.id).set(data).await()
    }

    suspend fun deleteTheory(theoryId: String) {
        collection.document(theoryId).delete().await()
    }
}
