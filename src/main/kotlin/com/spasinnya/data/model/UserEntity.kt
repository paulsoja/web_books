package com.spasinnya.data.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class UserEntity(
    val id: Long,
    val email: String,
    val passwordHash: String?,
    val status: String,
    val confirmedAt: Instant?,
    val lastLoginAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
)
