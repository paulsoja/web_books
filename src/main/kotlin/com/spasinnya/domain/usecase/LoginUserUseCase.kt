package com.spasinnya.domain.usecase

import com.spasinnya.domain.model.auth.LoginResult
import com.spasinnya.domain.port.PasswordHasher
import com.spasinnya.domain.port.TokenService
import com.spasinnya.domain.repository.RefreshTokenRepository
import com.spasinnya.domain.repository.UserRepository
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class LoginUserUseCase(
    private val users: UserRepository,
    private val refreshRepo: RefreshTokenRepository,
    private val hasher: PasswordHasher,
    private val tokens: TokenService
) {

    suspend operator fun invoke(
        email: String,
        rawPassword: String,
        userAgent: String? = null,
        ip: String? = null,
        deviceId: String? = null
    ): Result<LoginResult> = runCatching {
        val normalized = email.trim().lowercase()

        val user = users.findByEmail(normalized).getOrThrow() ?: error("Invalid credentials")
        require(!user.passwordHash.isNullOrBlank()) { "Password not set" }
        require(hasher.verify(rawPassword, user.passwordHash)) { "Invalid credentials" }
        require(user.status == "active") { "User is not active" }

        val issued = tokens.issueTokens(user.id, user.email)

        refreshRepo.save(
            userId = user.id,
            tokenHash = issued.refreshTokenHash,
            expiresAt = issued.refreshExpiresAt,
            userAgent = userAgent,
            ip = ip,
            deviceId = deviceId
        ).getOrThrow()

        users.updateLastLogin(user.id).getOrThrow()

        LoginResult(
            accessToken = issued.accessToken,
            accessExpiresAt = issued.accessExpiresAt,
            refreshToken = issued.refreshToken,
            refreshExpiresAt = issued.refreshExpiresAt
        )
    }
}