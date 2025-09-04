package com.spasinnya.domain.usecase

import com.spasinnya.data.extension.sha256
import com.spasinnya.domain.model.auth.RefreshResult
import com.spasinnya.domain.port.TokenService
import com.spasinnya.domain.repository.RefreshTokenRepository
import com.spasinnya.domain.repository.UserRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class RefreshSessionUseCase(
    private val users: UserRepository,
    private val refreshRepo: RefreshTokenRepository,
    private val tokens: TokenService
) {

    suspend operator fun invoke(
        refreshToken: String,   // ты передаёшь ХЭШ (а не сырое значение)
        userAgent: String? = null,
        ip: String? = null,
        deviceId: String? = null
    ): Result<RefreshResult> = runCatching {
        val hash = sha256(refreshToken)

        val saved = refreshRepo.findByHash(hash).getOrThrow() ?: error("Invalid refresh")
        require(saved.isActive) { "Refresh revoked" }

        val now = Clock.System.now()
        require(saved.expiresAt > now) { "Refresh expired" }

        val user = users.findById(saved.userId).getOrThrow() ?: error("User not found")
        require(user.status == "active") { "User is not active" }

        val issued = tokens.issueTokens(user.id, user.email)

        // Ротация refresh: ревокаем старый и сохраняем новый
        refreshRepo.revoke(saved.tokenHash).getOrThrow()
        refreshRepo.save(
            userId = user.id,
            tokenHash = issued.refreshTokenHash,
            expiresAt = issued.refreshExpiresAt,
            userAgent = userAgent,
            ip = ip,
            deviceId = deviceId
        ).getOrThrow()

        RefreshResult(
            accessToken = issued.accessToken,
            accessExpiresAt = issued.accessExpiresAt,
            refreshToken = issued.refreshToken,
            refreshExpiresAt = issued.refreshExpiresAt
        )
    }
}