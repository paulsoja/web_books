package com.spasinnya.domain.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class SetNewPasswordRequest(
    val email: String,
    val otpCode: String,
    val newPassword: String
)
