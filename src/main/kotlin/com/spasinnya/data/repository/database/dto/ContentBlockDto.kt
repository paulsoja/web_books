package com.spasinnya.data.repository.database.dto

import kotlinx.serialization.Serializable

@Serializable
data class ContentBlockDto(
    val type: String,
    val data: String
)
