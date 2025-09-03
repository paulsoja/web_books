package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object Users : Table("users") {
    val id = long("id").autoIncrement()
    val email = text("email").uniqueIndex()
    val passwordHash = text("password_hash").nullable()
    val status = text("status").default("pending")          // 'pending' | 'active' | 'blocked'
    val confirmedAt = timestamp("confirmed_at").nullable()
    val lastLoginAt = timestamp("last_login_at").nullable()
    val role = text("role").default("user")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}