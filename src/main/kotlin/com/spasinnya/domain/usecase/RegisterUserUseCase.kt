package com.spasinnya.domain.usecase

import com.spasinnya.domain.port.PasswordHasher
import com.spasinnya.domain.port.TransactionRunner
import com.spasinnya.domain.repository.UserRepository
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class RegisterUserUseCase(
    private val users: UserRepository,
    private val hasher: PasswordHasher,
    private val tx: TransactionRunner,
) {

    suspend operator fun invoke(email: String, rawPassword: String): Result<Unit> = runCatching {
        val normalized = email.trim().lowercase()

        val existing = users.findByEmail(normalized).getOrThrow()
        require(existing == null) { "User already exists" }

        tx.inTransaction {
            val pwdHash = hasher.hash(rawPassword)

            val userId = users.createUser(
                email = normalized,
                passwordHash = pwdHash,
                status = "pending"
            ).getOrThrow()

            users.createEmptyProfile(userId).getOrThrow()

            val otpCode = "1111"
            println("OTP for $normalized = $otpCode")
        }
    }
}