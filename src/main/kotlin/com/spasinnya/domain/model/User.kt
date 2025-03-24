package com.spasinnya.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val email: String,
    val password: String,
    val otpCode: String,
    val isConfirmed: Boolean
)
