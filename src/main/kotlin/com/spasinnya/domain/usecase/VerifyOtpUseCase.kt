package com.spasinnya.domain.usecase

import com.spasinnya.domain.repository.UserRepository
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class VerifyOtpUseCase(
    private val users: UserRepository
) {

    suspend operator fun invoke(email: String, code: String): Result<Unit> = runCatching {
        val normalized = email.trim().lowercase()
        require(code == "1111") { "Invalid code" }

        val user = users.findByEmail(normalized).getOrThrow() ?: error("User not found")
        users.setConfirmedAt(user.id).getOrThrow()
    }
}