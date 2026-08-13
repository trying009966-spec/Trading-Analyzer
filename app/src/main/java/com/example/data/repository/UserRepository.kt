package com.example.data.repository

import com.example.data.local.UserDao
import com.example.data.local.UserProfile
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

class UserRepository(private val userDao: UserDao) {

    val allUsers: Flow<List<UserProfile>> = userDao.getAllUsersFlow()

    suspend fun getUserByEmail(email: String): UserProfile? {
        return userDao.getUserByEmail(email.trim())
    }

    suspend fun getUserById(ownerId: String): UserProfile? {
        return userDao.getUserById(ownerId)
    }

    suspend fun registerUser(fullName: String, email: String, passwordRaw: String): Result<UserProfile> {
        val trimmedEmail = email.trim().lowercase()
        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return Result.failure(Exception("This email address is already registered."))
        }

        val hashedPassword = hashPassword(passwordRaw)
        val ownerId = "usr_" + System.currentTimeMillis() + "_" + (1000..9999).random()
        val newUser = UserProfile(
            owner_id = ownerId,
            full_name = fullName.trim(),
            email = trimmedEmail,
            password_hash = hashedPassword,
            status = "pending",
            registration_date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date())
        )

        userDao.insertUser(newUser)
        return Result.success(newUser)
    }

    suspend fun authenticateUser(email: String, passwordRaw: String): Result<UserProfile> {
        val trimmedEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return Result.failure(Exception("Invalid email or password."))

        val inputHash = hashPassword(passwordRaw)
        // Check hash or legacy fallback
        if (user.password_hash != inputHash && user.password_hash != passwordRaw) {
            return Result.failure(Exception("Invalid email or password."))
        }

        return Result.success(user)
    }

    suspend fun updateUserStatus(ownerId: String, status: String) {
        userDao.updateUserStatus(ownerId, status)
    }

    companion object {
        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
