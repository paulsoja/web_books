package com.spasinnya.data.repository

import com.spasinnya.data.extension.runDb
import com.spasinnya.data.repository.database.table.RefreshTokens
import com.spasinnya.domain.model.auth.RefreshToken
import com.spasinnya.domain.repository.RefreshTokenRepository
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class RefreshTokenDataRepository(
    private val database: Database
) : RefreshTokenRepository {

    override suspend fun save(
        userId: Long,
        tokenHash: String,
        expiresAt: Instant,
        userAgent: String?,
        ip: String?,
        deviceId: String?
    ): Result<Unit> = database.runDb {
        val now = Clock.System.now()
        RefreshTokens.insert {
            it[RefreshTokens.userId] = userId
            it[RefreshTokens.tokenHash] = tokenHash
            it[RefreshTokens.issuedAt] = now
            it[RefreshTokens.expiresAt] = expiresAt
            it[RefreshTokens.isActive] = true
            it[RefreshTokens.userAgent] = userAgent
            it[RefreshTokens.ip] = ip
            it[RefreshTokens.deviceId] = deviceId
        }
        Unit
    }

    override suspend fun findByHash(tokenHash: String): Result<RefreshToken?> = database.runDb {
        RefreshTokens
            .select(RefreshTokens.columns)
            .where { RefreshTokens.tokenHash eq tokenHash }
            .limit(1)
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun revoke(tokenHash: String): Result<Unit> = database.runDb {
        val now = Clock.System.now()
        RefreshTokens.update({ RefreshTokens.tokenHash eq tokenHash }) {
            it[RefreshTokens.isActive] = false
            it[RefreshTokens.revokedAt] = now
        }
        Unit
    }

    override suspend fun revokeAllForUser(userId: Long): Result<Unit> = database.runDb {
        val now = Clock.System.now()
        RefreshTokens.update({ RefreshTokens.userId eq userId }) {
            it[RefreshTokens.isActive] = false
            it[RefreshTokens.revokedAt] = now
        }
        Unit
    }

    //cleanupExpired() я сделал мягким (деактивация). Если хочешь физически удалять строки — замени update на deleteWhere
    override suspend fun cleanupExpired(): Result<Int> = database.runDb {
        val now = Clock.System.now()
        // пометим просроченные как неактивные; можно вернуть число обновлённых строк
        val updated = RefreshTokens.update({
            (RefreshTokens.expiresAt less now) and
                    (RefreshTokens.isActive eq true)
        }) {
            it[RefreshTokens.isActive] = false
        }
        updated
    }

    private fun ResultRow.toDomain() = RefreshToken(
        id         = this[RefreshTokens.id],
        userId     = this[RefreshTokens.userId],
        tokenHash  = this[RefreshTokens.tokenHash],
        issuedAt   = this[RefreshTokens.issuedAt],
        expiresAt  = this[RefreshTokens.expiresAt],
        revokedAt  = this[RefreshTokens.revokedAt],
        isActive   = this[RefreshTokens.isActive],
        userAgent  = this[RefreshTokens.userAgent],
        ip         = this[RefreshTokens.ip],
        deviceId   = this[RefreshTokens.deviceId]
    )
}