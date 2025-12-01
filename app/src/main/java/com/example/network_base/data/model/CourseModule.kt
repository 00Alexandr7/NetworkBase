package com.example.network_base.data.model

/**
 * Модуль курса
 */
data class CourseModule(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String = "ic_module",
    val lessons: List<Lesson> = emptyList(),
    val task: Task? = null,
    val order: Int = 0,
    val requiredModuleId: String? = null // Предыдущий модуль для разблокировки
) {
    /**
     * Получить общее количество уроков
     */
    fun getLessonCount(): Int = lessons.size
    
    /**
     * Проверить, есть ли практическое задание
     */
    fun hasTask(): Boolean = task != null
}

/**
 * Урок с теоретическим материалом
 */
data class Lesson(
    val id: String,
    val moduleId: String,
    val title: String,
    val order: Int = 0,
    val contentBlocks: List<ContentBlock> = emptyList(),
    val estimatedMinutes: Int = 5
)

/**
 * Блок контента в уроке
 */
sealed class ContentBlock {
    /**
     * Текстовый блок
     */
    data class Text(
        val content: String,
        val style: TextStyle = TextStyle.NORMAL
    ) : ContentBlock()
    
    /**
     * Блок с изображением
     */
    data class Image(
        val resourceName: String,
        val caption: String? = null,
        val altText: String = ""
    ) : ContentBlock()
    
    /**
     * Блок с кодом/примером
     */
    data class Code(
        val content: String,
        val language: String = "text"
    ) : ContentBlock()
    
    /**
     * Интерактивная схема
     */
    data class InteractiveSchema(
        val schemaId: String,
        val description: String = ""
    ) : ContentBlock()
    
    /**
     * Блок с важной информацией
     */
    data class InfoBox(
        val content: String,
        val type: InfoType = InfoType.INFO
    ) : ContentBlock()
    
    /**
     * Список (маркированный или нумерованный)
     */
    data class ListBlock(
        val items: List<String>,
        val ordered: Boolean = false
    ) : ContentBlock()
}

/**
 * Стиль текста
 */
enum class TextStyle {
    NORMAL,
    HEADING1,
    HEADING2,
    HEADING3,
    QUOTE
}

/**
 * Тип информационного блока
 */
enum class InfoType {
    INFO,      // Информация
    TIP,       // Совет
    WARNING,   // Предупреждение
    IMPORTANT  // Важно
}

/**
 * Практическое задание
 */
data class Task(
    val id: String,
    val moduleId: String,
    val title: String,
    val description: String,
    val objectives: List<String> = emptyList(), // Что нужно сделать
    val requirements: List<TaskRequirement> = emptyList(),
    val hints: List<TaskHint> = emptyList(),
    val initialTopology: NetworkTopology? = null, // Начальная топология (если есть)
    val maxScore: Int = 100,
    val xpReward: Int = 50
)

/**
 * Требование к заданию
 */
sealed class TaskRequirement {
    abstract val description: String
    abstract val errorMessage: String
    
    /**
     * Требование к количеству устройств
     */
    data class DeviceCount(
        val deviceType: DeviceType,
        val minCount: Int,
        val maxCount: Int? = null,
        override val description: String,
        override val errorMessage: String
    ) : TaskRequirement()
    
    /**
     * Требование к связности устройств
     */
    data class DevicesConnected(
        val deviceNames: List<String>,
        override val description: String,
        override val errorMessage: String
    ) : TaskRequirement()
    
    /**
     * Требование к настройке IP
     */
    data class IpConfigured(
        val subnet: String? = null, // Если указано, IP должен быть в этой подсети
        val deviceType: DeviceType? = null,
        override val description: String,
        override val errorMessage: String
    ) : TaskRequirement()
    
    /**
     * Требование к количеству подсетей
     */
    data class SubnetCount(
        val count: Int,
        override val description: String,
        override val errorMessage: String
    ) : TaskRequirement()
    
    /**
     * Требование к ping между устройствами
     */
    data class PingSuccessful(
        val fromDeviceName: String,
        val toDeviceName: String,
        override val description: String,
        override val errorMessage: String
    ) : TaskRequirement()
    
    /**
     * Кастомная проверка (для VLAN и др.)
     */
    data class Custom(
        val checkerId: String,
        override val description: String,
        override val errorMessage: String
    ) : TaskRequirement()
}

/**
 * Подсказка к заданию
 */
data class TaskHint(
    val id: String,
    val order: Int,
    val type: HintType,
    val title: String,
    val content: String,
    val relatedLessonId: String? = null // Ссылка на урок
)

/**
 * Тип подсказки
 */
enum class HintType {
    THEORY,     // Теоретическая (ссылка на урок)
    PRACTICAL,  // Практическая (что делать)
    STEP_BY_STEP // Пошаговая инструкция
}

