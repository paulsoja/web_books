package com.spasinnya.domain.model.auth

import com.spasinnya.data.repository.database.table.OtpPurpose
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class OtpRecord @OptIn(ExperimentalTime::class) constructor(
    val id: Long,
    val email: String,
    val purpose: OtpPurpose,
    val codeHash: String,
    val createdAt: Instant,
    val expiresAt: Instant,
    val consumedAt: Instant?,
    val attempts: Int
)
