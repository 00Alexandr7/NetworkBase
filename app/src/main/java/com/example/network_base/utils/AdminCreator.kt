package com.example.network_base.utils

/**
 * Утилитарный класс для создания администратора в системе
 * @deprecated Удалено в соответствии с требованиями к проекту
 */
@Deprecated("Функционал создания администраторов удален")
object AdminCreator {
    /**
     * @deprecated Метод больше не используется
     */
    @Deprecated("Метод больше не используется")
    suspend fun createAdmin(
        email: String,
        password: String,
        name: String
    ): Result<String> {
        return Result.failure(Exception("Создание администраторов запрещено"))
    }
}
