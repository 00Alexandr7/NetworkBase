package com.example.network_base.data.repository

import com.example.network_base.data.model.UserProfile
import com.example.network_base.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Репозиторий для управления пользователями администратором
 */
class UserManagementRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val usersCollection get() = firestore.collection("users")

    /**
     * Получить всех пользователей
     */
    suspend fun getAllUsers(): List<UserProfile> {
        val snapshot = usersCollection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            val id = doc.id
            val name = doc.getString("name") ?: "Пользователь"
            val email = doc.getString("email")
            val roleString = doc.getString("role") ?: "user"
            val role = UserRole.fromString(roleString)
            val xp = doc.getLong("xp")?.toInt() ?: 0
            val currentModuleId = doc.getString("currentModuleId")
            val createdAt = when {
                doc.get("createdAt") is com.google.firebase.Timestamp -> {
                    (doc.get("createdAt") as com.google.firebase.Timestamp).seconds * 1000
                }
                doc.getLong("createdAt") != null -> {
                    doc.getLong("createdAt")!!
                }
                else -> System.currentTimeMillis()
            }

            UserProfile(
                id = id,
                name = name,
                email = email,
                role = role,
                xp = xp,
                currentModuleId = currentModuleId,
                createdAt = createdAt
            )
        }
    }

    /**
     * Изменить роль пользователя
     */
    suspend fun updateUserRole(userId: String, newRole: String): Result<String> {
        return try {
            val roleEnum = UserRole.fromString(newRole)

            usersCollection.document(userId).update("role", roleEnum.name).await()
            Result.success("Роль пользователя успешно обновлена")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Удалить пользователя
     */
    suspend fun deleteUser(userId: String): Result<String> {
        return try {
            // Проверяем, что администратор не пытается удалить самого себя
            if (auth.currentUser?.uid == userId) {
                return Result.failure(Exception("Нельзя удалить собственный аккаунт"))
            }

            usersCollection.document(userId).delete().await()
            Result.success("Пользователь успешно удален")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
