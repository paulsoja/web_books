package com.spasinnya.domain.repository

import com.spasinnya.domain.model.auth.ActiveOtpToken
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
interface OtpTokenRepository {
    suspend fun upsertActive(email: String, purpose: String, codeHash: String, expiresAt: Instant, attempts: Int): Result<Unit>
    suspend fun fetchActive(email: String, purpose: String): Result<ActiveOtpToken?>
    suspend fun deactivate(email: String, purpose: String): Result<Unit>
    suspend fun decrementAttempts(email: String, purpose: String): Result<Int>
}