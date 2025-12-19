package com.example.network_base.data.model

import java.util.UUID

/**
 * Профиль пользователя
 */
data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "Пользователь",
    var email: String? = null,
    var role: UserRole = UserRole.GUEST,
    var xp: Int = 0,
    var currentModuleId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    // Поля для прогресса пользователя
    val completedModules: MutableMap<String, MutableSet<String>> = mutableMapOf(),
    var totalLessonsCompleted: Int = 0,
    val achievements: MutableSet<String> = mutableSetOf(),
    var totalAchievements: Int = 0,
    var tasksCompleted: Int = 0,
    var totalTimeSpent: Long = 0L,
    var networksCreated: Int = 0
) {
    // Добавляем метод cloneWithValues с нужными полями
    fun cloneWithValues(
        id: String = this.id,
        name: String = this.name,
        email: String? = this.email,
        role: UserRole = this.role,
        xp: Int = this.xp,
        currentModuleId: String? = this.currentModuleId,
        createdAt: Long = this.createdAt,
        completedModules: MutableMap<String, MutableSet<String>> = this.completedModules,
        totalLessonsCompleted: Int = this.totalLessonsCompleted,
        achievements: MutableSet<String> = this.achievements,
        totalAchievements: Int = this.totalAchievements,
        tasksCompleted: Int = this.tasksCompleted,
        totalTimeSpent: Long = this.totalTimeSpent,
        networksCreated: Int = this.networksCreated
    ): UserProfile = UserProfile(
        id, name, email, role, xp, currentModuleId, createdAt,
        completedModules, totalLessonsCompleted, achievements, totalAchievements,
        tasksCompleted, totalTimeSpent, networksCreated
    )
    /**
     * Получить текущий уровень на основе XP
     * Формула: level = sqrt(xp / 100)
     */
    fun getLevel(): Int {
        return kotlin.math.sqrt(xp / 100.0).toInt() + 1
    }
    
    /**
     * Получить XP, необходимый для следующего уровня
     */
    fun getXpForNextLevel(): Int {
        val nextLevel = getLevel() + 1
        return (nextLevel - 1) * (nextLevel - 1) * 100
    }
    
    /**
     * Получить XP для текущего уровня
     */
    fun getXpForCurrentLevel(): Int {
        val currentLevel = getLevel()
        return (currentLevel - 1) * (currentLevel - 1) * 100
    }
    
    /**
     * Получить прогресс до следующего уровня (0.0 - 1.0)
     */
    fun getLevelProgress(): Float {
        val currentLevelXp = getXpForCurrentLevel()
        val nextLevelXp = getXpForNextLevel()
        val xpInLevel = xp - currentLevelXp
        val xpNeeded = nextLevelXp - currentLevelXp
        
        return if (xpNeeded > 0) {
            (xpInLevel.toFloat() / xpNeeded).coerceIn(0f, 1f)
        } else {
            1f
        }
    }
    
    /**
     * Добавить XP
     */
    fun addXp(amount: Int): Int {
        val oldLevel = getLevel()
        xp += amount
        val newLevel = getLevel()
        return newLevel - oldLevel // Возвращает количество новых уровней
    }
    
    /**
     * Получить название уровня
     */
    fun getLevelTitle(): String {
        return when (getLevel()) {
            1 -> "Новичок"
            2 -> "Ученик"
            3 -> "Практикант"
            4 -> "Специалист"
            5 -> "Профессионал"
            6 -> "Эксперт"
            7 -> "Мастер"
            8 -> "Гуру"
            9 -> "Легенда"
            else -> "Сетевой Архитектор"
        }
    }
}

/**
 * Достижение
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val xpReward: Int = 25,
    val isSecret: Boolean = false // Скрытое достижение
) {
    companion object {
        // Предопределённые достижения
        val FIRST_STEP = Achievement(
            id = "first_step",
            title = "Первый шаг",
            description = "Завершить первое задание",
            iconName = "ic_achievement_first",
            xpReward = 25
        )
        
        val NETWORK_ENGINEER = Achievement(
            id = "network_engineer",
            title = "Сетевой инженер",
            description = "Завершить модуль \"Введение в сети\"",
            iconName = "ic_achievement_module1",
            xpReward = 50
        )
        
        val IP_MASTER = Achievement(
            id = "ip_master",
            title = "IP-мастер",
            description = "Завершить модуль \"IP-адресация\"",
            iconName = "ic_achievement_ip",
            xpReward = 50
        )
        
        val SWITCH_EXPERT = Achievement(
            id = "switch_expert",
            title = "Коммутатор",
            description = "Завершить модуль \"Коммутаторы и MAC\"",
            iconName = "ic_achievement_switch",
            xpReward = 50
        )
        
        val ROUTER_EXPERT = Achievement(
            id = "router_expert",
            title = "Маршрутизатор",
            description = "Завершить модуль \"Маршрутизация\"",
            iconName = "ic_achievement_router",
            xpReward = 50
        )
        
        val VLAN_GURU = Achievement(
            id = "vlan_guru",
            title = "VLAN-гуру",
            description = "Завершить модуль \"Практика VLAN\"",
            iconName = "ic_achievement_vlan",
            xpReward = 75
        )
        
        val PERFECTIONIST = Achievement(
            id = "perfectionist",
            title = "Перфекционист",
            description = "Получить 100% по всем заданиям",
            iconName = "ic_achievement_perfect",
            xpReward = 100
        )
        
        val EXPLORER = Achievement(
            id = "explorer",
            title = "Исследователь",
            description = "Создать сеть из 10+ устройств",
            iconName = "ic_achievement_explorer",
            xpReward = 50
        )
        
        /**
         * Все достижения
         */
        fun getAllAchievements(): List<Achievement> = listOf(
            FIRST_STEP,
            NETWORK_ENGINEER,
            IP_MASTER,
            SWITCH_EXPERT,
            ROUTER_EXPERT,
            VLAN_GURU,
            PERFECTIONIST,
            EXPLORER
        )
    }
}

/**
 * Разблокированное достижение пользователя
 */
data class UnlockedAchievement(
    val achievementId: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

/**
 * Прогресс пользователя по модулю
 */
data class ModuleProgress(
    val moduleId: String,
    val lessonsCompleted: MutableSet<String> = mutableSetOf(),
    var taskCompleted: Boolean = false,
    var taskScore: Int = 0,
    var taskAttempts: Int = 0,
    var bestScore: Int = 0
) {
    /**
     * Проверить, завершён ли модуль
     */
    fun isCompleted(totalLessons: Int): Boolean {
        return lessonsCompleted.size >= totalLessons && taskCompleted
    }
    
    /**
     * Получить прогресс по урокам (0.0 - 1.0)
     */
    fun getLessonsProgress(totalLessons: Int): Float {
        return if (totalLessons > 0) {
            lessonsCompleted.size.toFloat() / totalLessons
        } else {
            0f
        }
    }
    
    /**
     * Отметить урок как завершённый
     */
    fun completeLesson(lessonId: String) {
        lessonsCompleted.add(lessonId)
    }
    
    /**
     * Отметить задание как завершённое
     */
    fun completeTask(score: Int) {
        taskAttempts++
        taskScore = score
        if (score > bestScore) {
            bestScore = score
        }
        if (score >= 70) { // Минимум 70% для зачёта
            taskCompleted = true
        }
    }
}

/**
 * Сохранённая топология пользователя
 */
data class SavedTopology(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val topologyJson: String, // JSON сериализация NetworkTopology
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val taskId: String? = null // Если связано с заданием
)

