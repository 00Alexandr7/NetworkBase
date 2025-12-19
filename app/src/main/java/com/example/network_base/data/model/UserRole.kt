package com.example.network_base.data.model

/**
 * Роли пользователей в системе
 */
enum class UserRole {
    GUEST,
    USER,
    ADMIN
    ;

    companion object {
        fun fromString(value: String?): UserRole {
            val normalized = value
                ?.trim()
                ?.uppercase()
                ?.replace('Ё', 'Е')
                ?: return USER

            return when (normalized) {
                "ADMIN", "АДМИН", "АДМИНИСТРАТОР", "ADMINISTRATOR" -> ADMIN
                "GUEST", "ГОСТЬ" -> GUEST
                "USER", "ПОЛЬЗОВАТЕЛЬ" -> USER
                else -> runCatching { valueOf(normalized) }.getOrDefault(USER)
            }
        }
    }
}
