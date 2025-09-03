package com.spasinnya.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Long,
    val email: String,
    val status: String,
)
