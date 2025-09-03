package com.spasinnya.domain.model.auth

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class ActiveOtpToken(
    val email: String,
    val purpose: String,
    val codeHash: String,
    val expiresAt: Instant,
    val attemptsLeft: Int
)
