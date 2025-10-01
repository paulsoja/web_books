package com.spasinnya.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserProfileRequest(
    val firstName: String?,
    val lastName: String?
)
