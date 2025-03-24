package com.spasinnya.domain.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequest(
    val email: String
)
