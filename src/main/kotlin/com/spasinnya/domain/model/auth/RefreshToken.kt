package com.spasinnya.domain.model.auth

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class RefreshToken(
    val id: Long,
    val userId: Long,
    val tokenHash: String,
    val issuedAt: Instant,
    val expiresAt: Instant,
    val revokedAt: Instant?,
    val isActive: Boolean,
    val userAgent: String?,
    val ip: String?,
    val deviceId: String?
)
