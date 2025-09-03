package com.spasinnya.domain.usecase

import com.spasinnya.domain.port.PasswordHasher
import com.spasinnya.domain.repository.UserRepository
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class RegisterUserUseCase(
    private val users: UserRepository,
    private val hasher: PasswordHasher
) {

    suspend operator fun invoke(email: String, rawPassword: String): Result<Unit> = runCatching {
        val normalized = email.trim().lowercase()

        val existing = users.findByEmail(normalized).getOrThrow()
        require(existing == null) { "User already exists" }

        val pwdHash = hasher.hash(rawPassword)
        users.createUser(normalized, pwdHash, status = "pending").getOrThrow()

        // Мок-OTP
        val otpCode = "1111"
        println("OTP for $normalized = $otpCode")
    }
}