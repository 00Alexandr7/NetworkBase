package com.example.network_base.data.repository

import com.example.network_base.data.model.Lesson
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class LessonRepository(
    private val firestore: FirebaseFirestore
) {
    private val collection get() = firestore.collection("lessons")

    suspend fun loadLessons(): List<Lesson> {
        val snapshot = collection.get().await()
        return snapshot.documents.map { doc ->
            Lesson(
                id = doc.id,
                title = doc.getString("title").orEmpty(),
                description = doc.getString("description").orEmpty(),
                moduleId = doc.getString("moduleId").orEmpty(),
                content = doc.getString("content").orEmpty(),
                type = doc.getString("type").orEmpty(),
                order = doc.getLong("order")?.toInt() ?: 0,
                isPublished = doc.getBoolean("isPublished") ?: false,
                createdAt = when {
                    doc.get("createdAt") is com.google.firebase.Timestamp -> {
                        (doc.get("createdAt") as com.google.firebase.Timestamp).seconds * 1000
                    }
                    doc.getLong("createdAt") != null -> {
                        doc.getLong("createdAt")!!
                    }
                    else -> System.currentTimeMillis()
                },
                updatedAt = when {
                    doc.get("updatedAt") is com.google.firebase.Timestamp -> {
                        (doc.get("updatedAt") as com.google.firebase.Timestamp).seconds * 1000
                    }
                    doc.getLong("updatedAt") != null -> {
                        doc.getLong("updatedAt")!!
                    }
                    else -> System.currentTimeMillis()
                }
            )
        }.sortedBy { it.order }
    }

    suspend fun loadLessonsByModule(moduleId: String): List<Lesson> {
        val snapshot = collection
            .whereEqualTo("moduleId", moduleId)
            .get()
            .await()
        return snapshot.documents.map { doc ->
            Lesson(
                id = doc.id,
                title = doc.getString("title").orEmpty(),
                description = doc.getString("description").orEmpty(),
                moduleId = doc.getString("moduleId").orEmpty(),
                content = doc.getString("content").orEmpty(),
                type = doc.getString("type").orEmpty(),
                order = doc.getLong("order")?.toInt() ?: 0,
                isPublished = doc.getBoolean("isPublished") ?: false,
                createdAt = when {
                    doc.get("createdAt") is com.google.firebase.Timestamp -> {
                        (doc.get("createdAt") as com.google.firebase.Timestamp).seconds * 1000
                    }
                    doc.getLong("createdAt") != null -> {
                        doc.getLong("createdAt")!!
                    }
                    else -> System.currentTimeMillis()
                },
                updatedAt = when {
                    doc.get("updatedAt") is com.google.firebase.Timestamp -> {
                        (doc.get("updatedAt") as com.google.firebase.Timestamp).seconds * 1000
                    }
                    doc.getLong("updatedAt") != null -> {
                        doc.getLong("updatedAt")!!
                    }
                    else -> System.currentTimeMillis()
                }
            )
        }.sortedBy { it.order }
    }

    suspend fun createLesson(
        title: String,
        description: String,
        moduleId: String,
        content: String,
        type: String,
        order: Int,
        isPublished: Boolean = false
    ): String {
        val id = UUID.randomUUID().toString()
        val data = mapOf(
            "title" to title,
            "description" to description,
            "moduleId" to moduleId,
            "content" to content,
            "type" to type,
            "order" to order,
            "isPublished" to isPublished,
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis()
        )
        collection.document(id).set(data).await()
        return id
    }

    suspend fun updateLesson(lesson: Lesson) {
        val data = mapOf(
            "title" to lesson.title,
            "description" to lesson.description,
            "moduleId" to lesson.moduleId,
            "content" to lesson.content,
            "type" to lesson.type,
            "order" to lesson.order,
            "isPublished" to lesson.isPublished,
            "createdAt" to lesson.createdAt,
            "updatedAt" to System.currentTimeMillis()
        )
        collection.document(lesson.id).set(data).await()
    }

    suspend fun deleteLesson(lessonId: String) {
        collection.document(lessonId).delete().await()
    }

    suspend fun publishLesson(lessonId: String) {
        collection.document(lessonId)
            .update("isPublished", true)
            .await()
    }

    suspend fun unpublishLesson(lessonId: String) {
        collection.document(lessonId)
            .update("isPublished", false)
            .await()
    }
}
