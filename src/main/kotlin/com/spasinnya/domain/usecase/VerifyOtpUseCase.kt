package com.spasinnya.domain.usecase

import com.spasinnya.domain.model.auth.LoginResult
import com.spasinnya.domain.port.TokenService
import com.spasinnya.domain.repository.RefreshTokenRepository
import com.spasinnya.domain.repository.UserRepository
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class VerifyOtpUseCase(
    private val users: UserRepository,
    private val refresh: RefreshTokenRepository,
    private val tokens: TokenService
) {

    suspend operator fun invoke(
        email: String,
        code: String,
        userAgent: String? = null,
        ip: String? = null,
        deviceId: String? = null
    ): Result<LoginResult> = runCatching {
        val normalized = email.trim().lowercase()
        require(code == "1111") { "Invalid code" }

        val user = users.findByEmail(normalized).getOrThrow() ?: error("User not found")

        users.setConfirmedAt(user.id).getOrThrow()
        users.updateLastLogin(user.id).getOrThrow()

        val issued = tokens.issueTokens(user.id, user.email)

        refresh.save(
            userId = user.id,
            tokenHash = issued.refreshTokenHash,
            expiresAt = issued.refreshExpiresAt,   // kotlin.time.Instant (доменные типы)
            userAgent = userAgent,
            ip = ip,
            deviceId = deviceId
        ).getOrThrow()

        LoginResult(
            accessToken = issued.accessToken,
            accessExpiresAt = issued.accessExpiresAt,
            refreshToken = issued.refreshToken,
            refreshExpiresAt = issued.refreshExpiresAt
        )
    }
}