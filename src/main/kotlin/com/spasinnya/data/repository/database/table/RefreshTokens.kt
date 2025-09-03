package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object RefreshTokens : Table("refresh_tokens") {
    val id = long("id").autoIncrement()
    val userId = long("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val tokenHash = text("token_hash").uniqueIndex()
    val issuedAt = timestamp("issued_at").defaultExpression(CurrentTimestamp)
    val expiresAt = timestamp("expires_at")
    val revokedAt = timestamp("revoked_at").nullable()
    val isActive = bool("is_active").default(true)
    val userAgent = text("user_agent").nullable()
    val ip = text("ip").nullable()
    val deviceId = text("device_id").nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        index(isUnique = false, columns = arrayOf(userId, isActive))
        index(isUnique = false, columns = arrayOf(expiresAt))
    }
}