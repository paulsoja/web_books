package com.spasinnya.domain.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpRequest(
    val email: String,
    val otpCode: String
)
