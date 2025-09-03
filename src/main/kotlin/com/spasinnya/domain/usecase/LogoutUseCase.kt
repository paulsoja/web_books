package com.spasinnya.domain.usecase

import com.spasinnya.domain.repository.RefreshTokenRepository

class LogoutUseCase(
    private val refreshRepo: RefreshTokenRepository
) {

    /** Логаут с одного устройства (по конкретному refresh-куку). */
    suspend fun revokeSingle(refreshTokenHash: String): Result<Unit> =
        refreshRepo.revoke(refreshTokenHash)

    /** Логаут со всех устройств. */
    suspend fun revokeAll(userId: Long): Result<Unit> =
        refreshRepo.revokeAllForUser(userId)
}