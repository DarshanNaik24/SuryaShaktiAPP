package com.suryashakti.solarmonitor.repository

import com.suryashakti.solarmonitor.data.SessionManager
import com.suryashakti.solarmonitor.data.User
import com.suryashakti.solarmonitor.data.UserDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {
    val currentUserId: Flow<String?> = sessionManager.userId

    val currentUser: Flow<User?> = currentUserId.map {
        if (it != null) {
            userDao.getUserById(it)
        } else {
            null
        }
    }

    suspend fun login(userId: String, password: String): Result<User> {
        val user = userDao.getUserById(userId)
        return when {
            user == null -> Result.failure(Exception("User not found"))
            user.password != password -> Result.failure(Exception("Incorrect password"))
            else -> {
                sessionManager.saveUserId(userId)
                Result.success(user)
            }
        }
    }

    suspend fun signup(user: User): Result<Unit> {
        return try {
            userDao.registerUser(user)
            sessionManager.saveUserId(user.userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("User ID already exists"))
        }
    }

    suspend fun changePassword(userId: String, oldPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = userDao.getUserById(userId)
            when {
                user == null -> Result.failure(Exception("User not found"))
                user.password != oldPassword -> Result.failure(Exception("Incorrect old password"))
                else -> {
                    userDao.updateUser(user.copy(password = newPassword))
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to change password"))
        }
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }
}
