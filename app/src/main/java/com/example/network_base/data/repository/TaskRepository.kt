package com.example.network_base.data.repository

import com.example.network_base.data.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class TaskRepository(
    private val firestore: FirebaseFirestore
) {
    private val collection get() = firestore.collection("tasks")

    suspend fun loadTasks(): List<Task> {
        val snapshot = collection.get().await()
        return snapshot.documents.map { doc ->
            Task(
                id = doc.id,
                title = doc.getString("title").orEmpty(),
                lessonId = doc.getString("lessonId").orEmpty(),
                type = doc.getString("type").orEmpty(),
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
        }.sortedBy { it.createdAt }
    }

    suspend fun loadTasksByLesson(lessonId: String): List<Task> {
        val snapshot = collection
            .whereEqualTo("lessonId", lessonId)
            .get()
            .await()
        return snapshot.documents.map { doc ->
            Task(
                id = doc.id,
                title = doc.getString("title").orEmpty(),
                lessonId = doc.getString("lessonId").orEmpty(),
                type = doc.getString("type").orEmpty(),
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
        }.sortedBy { it.createdAt }
    }

    suspend fun createTask(
        title: String,
        lessonId: String,
        type: String,
        isPublished: Boolean = false
    ): String {
        val id = UUID.randomUUID().toString()
        val data = mapOf(
            "title" to title,
            "lessonId" to lessonId,
            "type" to type,
            "isPublished" to isPublished,
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis()
        )
        collection.document(id).set(data).await()
        return id
    }

    suspend fun updateTask(task: Task) {
        val data = mapOf(
            "title" to task.title,
            "lessonId" to task.lessonId,
            "type" to task.type,
            "isPublished" to task.isPublished,
            "createdAt" to task.createdAt,
            "updatedAt" to System.currentTimeMillis()
        )
        collection.document(task.id).set(data).await()
    }

    suspend fun deleteTask(taskId: String) {
        collection.document(taskId).delete().await()
    }

    suspend fun publishTask(taskId: String) {
        collection.document(taskId)
            .update("isPublished", true)
            .await()
    }

    suspend fun unpublishTask(taskId: String) {
        collection.document(taskId)
            .update("isPublished", false)
            .await()
    }
}
