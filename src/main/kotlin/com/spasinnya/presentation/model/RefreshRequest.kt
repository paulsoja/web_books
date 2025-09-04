package com.spasinnya.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequest(val refreshToken: String)
