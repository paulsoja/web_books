package com.spasinnya.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val reason: String,
    val statusCode: String,
)
