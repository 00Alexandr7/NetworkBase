package com.example.network_base.data.model

/**
 * Теоретический материал для урока
 */
data class Theory(
    val id: String = "",
    val lessonId: String = "",
    val moduleId: String = "",
    val title: String = "",
    val content: String = "",
    val order: Int = 0
)
