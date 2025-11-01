package com.spasinnya.data.repository

import com.spasinnya.data.extension.runDb
import com.spasinnya.data.repository.database.table.OtpTokens
import com.spasinnya.domain.model.auth.ActiveOtpToken
import com.spasinnya.domain.repository.OtpTokenRepository
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.minus
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class OtpTokenDataRepository(
    private val database: Database
) : OtpTokenRepository {

    override suspend fun upsertActive(
        email: String,
        purpose: String,
        codeHash: String,
        expiresAt: Instant,
        attempts: Int
    ): Result<Unit> = database.runDb {
        // 1) deactivate previous active
        OtpTokens.update({
            (OtpTokens.email eq email) and
                    (OtpTokens.purpose eq purpose) and
                    (OtpTokens.isActive eq true)
        }) {
            it[OtpTokens.isActive] = false
        }

        // 2) try to update current
        val updated = OtpTokens.update({
            (OtpTokens.email eq email) and
                    (OtpTokens.purpose eq purpose)
        }) {
            it[OtpTokens.codeHash] = codeHash
            it[OtpTokens.expiresAt] = expiresAt
            it[OtpTokens.attemptsLeft] = attempts
            it[OtpTokens.isActive] = true
        }

        // 3) insert if not updated
        if (updated == 0) {
            OtpTokens.insert {
                it[OtpTokens.email] = email
                it[OtpTokens.purpose] = purpose
                it[OtpTokens.codeHash] = codeHash
                it[OtpTokens.expiresAt] = expiresAt
                it[OtpTokens.attemptsLeft] = attempts
                it[OtpTokens.isActive] = true
            }
        }
    }

    override suspend fun fetchActive(
        email: String,
        purpose: String
    ): Result<ActiveOtpToken?> = database.runDb {
        OtpTokens
            .select(OtpTokens.columns)
            .where {
                (OtpTokens.email eq email) and
                        (OtpTokens.purpose eq purpose) and
                        (OtpTokens.isActive eq true)
            }
            .limit(1)
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun deactivate(email: String, purpose: String): Result<Unit> = database.runDb {
        OtpTokens.update({
            (OtpTokens.email eq email) and
                    (OtpTokens.purpose eq purpose) and
                    (OtpTokens.isActive eq true)
        }) { it[OtpTokens.isActive] = false }
        Unit
    }

    override suspend fun decrementAttempts(email: String, purpose: String): Result<Int> = database.runDb {
        // decrease attempts_left, if > 0
        OtpTokens.update({
            (OtpTokens.email eq email) and
                    (OtpTokens.purpose eq purpose) and
                    (OtpTokens.isActive eq true) and
                    (OtpTokens.attemptsLeft greater 0)
        }) {
            it[OtpTokens.attemptsLeft] = OtpTokens.attemptsLeft - 1
        }

        // read the current value (or 0, if not found)
        OtpTokens
            .select(OtpTokens.columns)
            .where {
                (OtpTokens.email eq email) and
                        (OtpTokens.purpose eq purpose) and
                        (OtpTokens.isActive eq true)
            }
            .limit(1)
            .firstOrNull()
            ?.get(OtpTokens.attemptsLeft)
            ?: 0
    }

    private fun ResultRow.toDomain() = ActiveOtpToken(
        email        = this[OtpTokens.email],
        purpose      = this[OtpTokens.purpose],
        codeHash     = this[OtpTokens.codeHash],
        expiresAt    = this[OtpTokens.expiresAt],
        attemptsLeft = this[OtpTokens.attemptsLeft]
    )
}