package com.spasinnya.domain.model.auth

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class IssuedTokens(
    val accessToken: String,
    val accessExpiresAt: Instant,
    val refreshToken: String,      // сырое значение -> клиенту
    val refreshTokenHash: String,  // храним в БД
    val refreshExpiresAt: Instant
)
