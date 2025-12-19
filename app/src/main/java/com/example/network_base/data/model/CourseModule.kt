
package com.example.network_base.data.model

/**
 * Модуль курса
 */
data class CourseModule(
    val id: String,
    val title: String,
    val description: String,
    val order: Int,
    val lessons: List<LessonWithContent> = emptyList(),
    val task: TaskWithRequirements? = null
) {
    /**
     * Получить количество уроков в модуле
     */
    fun getLessonCount(): Int = lessons.size
}

/**
 * Типы информационных блоков в уроке
 */
enum class InfoType {
    TEXT,
    IMAGE,
    DIAGRAM,
    CODE,
    NOTE,
    WARNING,
    TIP,
    EXAMPLE,
    EXERCISE
}

/**
 * Стили текста
 */
enum class TextStyle {
    NORMAL,
    BOLD,
    ITALIC,
    CODE,
    HEADING_1,
    HEADING_2,
    HEADING_3,
    QUOTE
}

/**
 * Информационный блок в уроке
 */
data class ContentBlock(
    val type: InfoType,
    val content: String,
    val style: TextStyle = TextStyle.NORMAL,
    val caption: String? = null,
    val resourceName: String? = null, // Для изображений и диаграмм
    val items: List<String> = emptyList(), // Для списков
    val ordered: Boolean = false // Для списков
)

/**
 * Расширенный урок с содержимым
 */
data class LessonWithContent(
    val id: String,
    val moduleId: String,
    val title: String,
    val description: String,
    val order: Int,
    val estimatedMinutes: Int,
    val contentBlocks: List<ContentBlock> = emptyList()
)

/**
 * Расширенное задание с требованиями
 */
data class TaskWithRequirements(
    val id: String,
    val moduleId: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val requirements: List<TaskRequirement>,
    val initialTopology: NetworkTopology? = null,
    val hints: List<String> = emptyList(),
    val objectives: List<String> = emptyList()
)
