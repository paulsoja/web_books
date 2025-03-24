package com.spasinnya.domain.repository

import com.spasinnya.domain.model.User

interface UserRepository {
    suspend fun createUser(email: String, password: String, otpCode: String): User
    suspend fun findByEmail(email: String): User?
    suspend fun confirmUser(id: Int)
    suspend fun updatePassword(email: String, newPassword: String)
}