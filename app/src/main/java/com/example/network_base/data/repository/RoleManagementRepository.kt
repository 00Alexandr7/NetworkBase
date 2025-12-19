package com.example.network_base.data.repository

import com.example.network_base.data.model.UserProfile
import com.example.network_base.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Репозиторий для управления ролями пользователей
 */
class RoleManagementRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Назначить роль пользователю
     */
    suspend fun assignRole(userId: String, role: UserRole): Boolean {
        return try {
            // Проверяем, что текущий пользователь - администратор
            if (!isAdmin()) return false

            firestore.collection("users")
                .document(userId)
                .update("role", role.name)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Получить роль пользователя
     */
    suspend fun getUserRole(userId: String): UserRole? {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val roleString = document.getString("role") ?: "guest"
            UserRole.fromString(roleString)
        } catch (e: Exception) {
            UserRole.GUEST
        }
    }

    /**
     * Проверить, является ли текущий пользователь администратором
     */
    suspend fun isAdmin(): Boolean {
        return try {
            val uid = auth.currentUser?.uid ?: return false
            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            UserRole.fromString(document.getString("role")) == UserRole.ADMIN
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Получить список всех пользователей с их ролями
     */
    suspend fun getAllUsers(): List<Pair<UserProfile, UserRole>> {
        return try {
            if (!isAdmin()) return emptyList()

            val documents = firestore.collection("users")
                .get()
                .await()
                .documents

            documents.mapNotNull { doc ->
                val userProfile = doc.toObject(UserProfile::class.java)
                val roleString = doc.getString("role") ?: "guest"
                val role = UserRole.fromString(roleString)

                userProfile?.let { user: UserProfile -> Pair(user, role) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
