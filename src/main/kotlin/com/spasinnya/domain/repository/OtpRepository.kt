package com.spasinnya.domain.repository

import com.spasinnya.data.repository.database.dto.OtpRow
import com.spasinnya.data.repository.database.table.OtpPurpose
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
interface OtpRepository {
    /** Возвращает true, если можно запрашивать OTP (прошёл cooldown). */
    suspend fun canRequest(email: String, purpose: OtpPurpose, now: Instant, cooldownSeconds: Long): Result<Boolean>

    /** Создаёт запись OTP (hash + expiresAt) и возвращает id */
    suspend fun create(
        email: String,
        purpose: OtpPurpose,
        codeHash: String,
        createdAt: Instant,
        expiresAt: Instant,
        ip: String?,
        ua: String?
    ): Result<Long>
    suspend fun findLatestActive(email: String, purpose: OtpPurpose, now: Instant): Result<OtpRow?>
    suspend fun incrementAttempts(id: Long): Result<Unit>
    suspend fun consume(id: Long, at: Instant): Result<Unit>
}