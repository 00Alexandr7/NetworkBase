package com.example.network_base.data.repository

import com.example.network_base.data.local.dao.AchievementDao
import com.example.network_base.data.local.dao.ProgressDao
import com.example.network_base.data.local.dao.UserDao
import com.example.network_base.data.local.entities.ProgressEntity
import com.example.network_base.data.local.entities.UserEntity
import com.example.network_base.data.model.UserProfile
import com.example.network_base.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Работа с Firebase Auth/Firestore и синхронизацией локальных данных
 */
class AuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao,
    private val progressDao: ProgressDao,
    private val achievementDao: AchievementDao
)
{
    suspend fun isEmailFree(email: String): Boolean {
        val methods = auth.fetchSignInMethodsForEmail(email).await()
        return methods.signInMethods.isNullOrEmpty()
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        transferGuest: Boolean
    ): UserProfile {
        if (!isEmailFree(email)) throw FirebaseAuthUserCollisionException("auth/email-already-in-use", "Email already exists")

        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: error("Не удалось создать пользователя")

        val localUser = userDao.getUser()
        val profile = buildProfileForRemote(
            uid = uid,
            name = name,
            email = email,
            role = UserRole.USER,
//            disabled = false,
            xp = if (transferGuest) localUser?.xp ?: 0 else 0,
            currentModuleId = if (transferGuest) localUser?.currentModuleId else null,
            createdAt = System.currentTimeMillis()
        )

        saveProfileRemote(uid, profile)
        if (transferGuest) {
            pushLocalProgress(uid)
        }
        cacheProfile(profile)
        if (!transferGuest) {
            progressDao.deleteAll()
            achievementDao.deleteAll()
        }
        return profile
    }

    suspend fun login(
        email: String,
        password: String,
        transferGuest: Boolean
    ): UserProfile {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: error("Не удалось войти")

        val localUser = userDao.getUser()
        if (transferGuest) {
            pushLocalProgress(uid)
        }

        val remoteProfile = loadProfile(uid) ?: createFallbackProfile(uid, email)
        val merged = if (transferGuest && localUser != null) {
            remoteProfile.copy(
                name = localUser.name,
                xp = localUser.xp,
                currentModuleId = localUser.currentModuleId
            ).also { saveProfileRemote(uid, it) }
        } else if (localUser != null && remoteProfile.name == "Пользователь") {
            // Если в Firebase имя по умолчанию, используем локальное
            remoteProfile.copy(name = localUser.name)
        } else {
            remoteProfile
        }

        cacheProfile(merged)
        if (!transferGuest) {
            progressDao.deleteAll()
            achievementDao.deleteAll()
        }
        pullRemoteProgress(uid)
        return merged
    }

    suspend fun logout() {
        // Сначала пытаемся синхронизировать прогресс зарегистрированного пользователя
        val firebaseUser = auth.currentUser
        val uid = firebaseUser?.uid
        val localUser = userDao.getUser()

        if (uid != null && localUser != null) {
            val profile = localUser.toUserProfile()
            if (profile.role != UserRole.GUEST) {
                runCatching {
                    saveProfileRemote(uid, profile)
                    pushLocalProgress(uid)
                }
            }
        }

        auth.signOut()
        // Очищаем локальные данные пользователя
        userDao.deleteAll()
        progressDao.deleteAll()
        achievementDao.deleteAll()
    }

    suspend fun currentProfile(): UserProfile? {
        return userDao.getUser()?.toUserProfile()
    }

    suspend fun checkAdminRole(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val snapshot = firestore.collection("users").document(uid).get().await()
        return UserRole.fromString(snapshot.getString("role")) == UserRole.ADMIN
    }

    private suspend fun createFallbackProfile(uid: String, email: String): UserProfile {
        val profile = buildProfileForRemote(
            uid = uid,
            name = email.substringBefore('@'),
            email = email,
            role = UserRole.USER,
//            disabled = false,
            xp = 0,
            currentModuleId = null,
            createdAt = System.currentTimeMillis()
        )
        saveProfileRemote(uid, profile)
        return profile
    }

    private suspend fun saveProfileRemote(uid: String, profile: UserProfile) {
        val data = mapOf(
            "name" to profile.name,
            "email" to profile.email,
            "role" to profile.role.name,
            "xp" to profile.xp,
            "currentModuleId" to profile.currentModuleId,
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        firestore.collection("users").document(uid).set(data).await()
    }

    private suspend fun loadProfile(uid: String): UserProfile? {
        val snapshot = firestore.collection("users").document(uid).get().await()
        if (!snapshot.exists()) return null
        val name = snapshot.getString("name") ?: "Пользователь"
        val email = snapshot.getString("email")
        val roleString = snapshot.getString("role") ?: "user"
        val role = UserRole.fromString(roleString)
        val xp = snapshot.getLong("xp")?.toInt() ?: 0
        val currentModuleId = snapshot.getString("currentModuleId")
        // Изменить способ получения createdAt для поддержки Timestamp
        val createdAt = when {
            snapshot.get("createdAt") is com.google.firebase.Timestamp -> {
                (snapshot.get("createdAt") as com.google.firebase.Timestamp).seconds * 1000
            }
            snapshot.getLong("createdAt") != null -> {
                snapshot.getLong("createdAt")!!
            }
            else -> System.currentTimeMillis()
        }

        return buildProfileForRemote(uid, name, email, role, xp, currentModuleId, createdAt)
    }

    private suspend fun pushLocalProgress(uid: String) {
        val progresses = progressDao.getAllProgress()
        val userRef = firestore.collection("users").document(uid)
        progresses.forEach { entity ->
            val moduleRef = userRef.collection("progress").document(entity.moduleId)
            val lessons = entity.toModuleProgress().lessonsCompleted.toList()
            val payload = mapOf(
                "moduleId" to entity.moduleId,
                "lessonsCompleted" to lessons,
                "taskCompleted" to entity.taskCompleted,
                "taskScore" to entity.taskScore,
                "taskAttempts" to entity.taskAttempts,
                "bestScore" to entity.bestScore
            )
            moduleRef.set(payload).await()
        }
    }

    private suspend fun pullRemoteProgress(uid: String) {
        val userRef = firestore.collection("users").document(uid)
        val snapshot = userRef.collection("progress").get().await()
        progressDao.deleteAll()
        snapshot.documents.forEach { doc ->
            val moduleId = doc.getString("moduleId") ?: doc.id
            val lessons = doc.get("lessonsCompleted") as? List<String> ?: emptyList()
            val entity = ProgressEntity(
                moduleId = moduleId,
                lessonsCompletedJson = com.google.gson.Gson().toJson(lessons.toSet()),
                taskCompleted = doc.getBoolean("taskCompleted") ?: false,
                taskScore = (doc.getLong("taskScore") ?: 0).toInt(),
                taskAttempts = (doc.getLong("taskAttempts") ?: 0).toInt(),
                bestScore = (doc.getLong("bestScore") ?: 0).toInt()
            )
            progressDao.insertProgress(entity)
        }
    }

    private suspend fun cacheProfile(profile: UserProfile) {
        userDao.deleteAll()
        userDao.insertUser(UserEntity.fromUserProfile(profile))
    }

    private fun buildProfileForRemote(
        uid: String,
        name: String,
        email: String?,
        role: UserRole,
        xp: Int,
        currentModuleId: String?,
        createdAt: Long
    ): UserProfile {
        return UserProfile(
            id = uid,
            name = name,
            email = email,
            role = role,
            xp = xp,
            currentModuleId = currentModuleId,
            createdAt = createdAt
        )
    }
}

