package com.spasinnya.domain.usecase

import com.spasinnya.data.extension.sha256
import com.spasinnya.domain.port.TokenService
import com.spasinnya.domain.repository.RefreshTokenRepository

class LogoutUseCase(
    private val refreshRepo: RefreshTokenRepository,
    private val tokens: TokenService
) {

    /** Логаут с одного устройства (по конкретному refresh-куку). */
    suspend fun revokeSingleRaw(refreshToken: String): Result<Unit> {
        val hash = sha256(refreshToken)
        return refreshRepo.revoke(hash)
    }

    /** Логаут со всех устройств. */
    suspend fun revokeAll(userId: Long): Result<Unit> =
        refreshRepo.revokeAllForUser(userId)
}