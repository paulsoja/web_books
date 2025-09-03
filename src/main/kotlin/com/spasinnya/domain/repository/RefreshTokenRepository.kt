package com.spasinnya.domain.repository

import com.spasinnya.domain.model.auth.RefreshToken
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
interface RefreshTokenRepository {
    suspend fun save(userId: Long, tokenHash: String, expiresAt: Instant, userAgent: String?, ip: String?, deviceId: String?): Result<Unit>
    suspend fun findByHash(tokenHash: String): Result<RefreshToken?>
    suspend fun revoke(tokenHash: String): Result<Unit>
    suspend fun revokeAllForUser(userId: Long): Result<Unit>
    suspend fun cleanupExpired(): Result<Int>
}