package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.between
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object OtpTokens : Table("otp_tokens") {
    val email = text("email")
    val purpose = text("purpose")                           // REGISTER | LOGIN | RESET_PASSWORD
    val codeHash = text("code_hash")
    val expiresAt = timestamp("expires_at")
    val attemptsLeft = integer("attempts_left")
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(email, purpose)

    init {
        index(isUnique = false, columns = arrayOf(email, purpose, isActive))
        index(isUnique = false, columns = arrayOf(expiresAt))
        check { attemptsLeft.between(0, 10) }
    }
}