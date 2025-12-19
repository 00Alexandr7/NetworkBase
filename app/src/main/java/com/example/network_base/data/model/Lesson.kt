package com.example.network_base.data.model

/**
 * Урок курса
 */
data class Lesson(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val moduleId: String = "",
    val content: String = "",
    val type: String = "",
    val order: Int = 0,
    val isPublished: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
