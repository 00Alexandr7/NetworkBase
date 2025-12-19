package com.example.network_base.data.model

/**
 * Задание для урока
 */
data class Task(
    val id: String = "",
    val title: String = "",
    val lessonId: String = "",
    val type: String = "",
    val isPublished: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
