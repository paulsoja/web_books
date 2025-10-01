package com.spasinnya.domain.repository

import com.spasinnya.domain.model.User
import com.spasinnya.domain.model.UserProfile

interface UserRepository {
    suspend fun createUser(email: String, passwordHash: String?, status: String = "pending"): Result<Long>
    suspend fun findByEmail(email: String): Result<User?>
    suspend fun findById(id: Long): Result<User?>
    suspend fun setConfirmedAt(userId: Long): Result<Unit>
    suspend fun updatePassword(userId: Long, passwordHash: String): Result<Unit>
    suspend fun updateLastLogin(userId: Long): Result<Unit>
    suspend fun createEmptyProfile(userId: Long): Result<Unit>
    suspend fun findUserProfile(userId: Long): Result<UserProfile?>
    suspend fun updateProfile(userId: Long, firstName: String?, lastName: String?): Result<Unit>
}