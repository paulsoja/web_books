package com.spasinnya.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val userId: Long,
    val email: String,
    val role: String,
    val firstName: String,
    val lastName: String,
    val avatarUrl: String,
)
