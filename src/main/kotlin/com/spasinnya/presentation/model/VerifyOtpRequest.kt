package com.spasinnya.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpRequest(
    val email: String,
    val code: String
)
