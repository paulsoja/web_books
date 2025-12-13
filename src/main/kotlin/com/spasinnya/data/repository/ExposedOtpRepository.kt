package com.spasinnya.data.repository

import com.spasinnya.data.repository.database.dto.OtpRow
import com.spasinnya.data.repository.database.table.EmailOtps
import com.spasinnya.data.repository.database.table.OtpPurpose
import com.spasinnya.domain.repository.OtpRepository
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class ExposedOtpRepository : OtpRepository {

    override suspend fun canRequest(
        email: String,
        purpose: OtpPurpose,
        now: Instant,
        cooldownSeconds: Long
    ): Result<Boolean> = runCatching {
        val normalized = email.lowercase()
        val cooldown = cooldownSeconds.seconds

        transaction {
            val lastCreatedAt: Instant? = EmailOtps
                .select(EmailOtps.createdAt)
                .where { (EmailOtps.email eq normalized) and (EmailOtps.purpose eq purpose.name) }
                .orderBy(EmailOtps.id to SortOrder.DESC)
                .limit(1)
                .singleOrNull()
                ?.get(EmailOtps.createdAt)

            if (lastCreatedAt == null) return@transaction true

            val nextAllowedAt = lastCreatedAt + cooldown
            now >= nextAllowedAt
        }
    }

    override suspend fun create(
        email: String,
        purpose: OtpPurpose,
        codeHash: String,
        createdAt: Instant,
        expiresAt: Instant,
        ip: String?,
        ua: String?
    ): Result<Long> = runCatching {
        val normalized = email.lowercase()

        transaction {
            EmailOtps.insertAndGetId { st ->
                st[EmailOtps.email] = normalized
                st[EmailOtps.purpose] = purpose.name
                st[EmailOtps.codeHash] = codeHash
                st[EmailOtps.createdAt] = createdAt
                st[EmailOtps.expiresAt] = expiresAt
                st[EmailOtps.requestIp] = ip
                st[EmailOtps.userAgent] = ua
                // attempts default 0, consumedAt null
            }.value
        }
    }

    override suspend fun findLatestActive(email: String, purpose: OtpPurpose, now: Instant): Result<OtpRow?> =
        runCatching {
            val normalized = email.lowercase()
            transaction {
                EmailOtps
                    .selectAll()
                    .where {
                        (EmailOtps.email eq normalized) and
                                (EmailOtps.purpose eq purpose.name) and
                                EmailOtps.consumedAt.isNull() and
                                (EmailOtps.expiresAt greater now)
                    }
                    .orderBy(EmailOtps.id to SortOrder.DESC)
                    .limit(1)
                    .singleOrNull()
                    ?.let { r ->
                        OtpRow(
                            id = r[EmailOtps.id].value,
                            email = r[EmailOtps.email],
                            purpose = OtpPurpose.valueOf(r[EmailOtps.purpose]),
                            codeHash = r[EmailOtps.codeHash],
                            expiresAt = r[EmailOtps.expiresAt],
                            consumedAt = r[EmailOtps.consumedAt],
                            attempts = r[EmailOtps.attempts]
                        )
                    }
            }
        }

    override suspend fun incrementAttempts(id: Long): Result<Unit> = runCatching {
        transaction {
            EmailOtps.update({ EmailOtps.id eq id }) {
                it[attempts] = EmailOtps.attempts + 1
            }
        }
    }

    override suspend fun consume(id: Long, at: Instant): Result<Unit> = runCatching {
        transaction {
            EmailOtps.update({ EmailOtps.id eq id }) {
                it[consumedAt] = at
            }
        }
    }
}