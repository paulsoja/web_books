package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime

enum class OtpPurpose { LOGIN }

@OptIn(ExperimentalTime::class)
object EmailOtps : LongIdTable("email_otp") {
    val email = text("email")
    val purpose = text("purpose")
    val codeHash = text("code_hash")

    // ВАЖНО: это будет kotlin.time.Instant (в актуальной линейке exposed-kotlin-datetime) :contentReference[oaicite:2]{index=2}
    val createdAt = timestamp("created_at")
    val expiresAt = timestamp("expires_at")
    val consumedAt = timestamp("consumed_at").nullable()

    val attempts = integer("attempts").default(0)
    val requestIp = text("request_ip").nullable()
    val userAgent = text("user_agent").nullable()
}
