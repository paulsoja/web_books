package com.spasinnya.data.repository.database.dto

import com.spasinnya.data.repository.database.table.OtpPurpose
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class OtpRow @OptIn(ExperimentalTime::class) constructor(
    val id: Long,
    val email: String,
    val purpose: OtpPurpose,
    val codeHash: String,
    val expiresAt: Instant,
    val consumedAt: Instant?,
    val attempts: Int
)
