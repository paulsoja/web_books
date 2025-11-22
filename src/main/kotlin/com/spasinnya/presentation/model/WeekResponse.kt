package com.spasinnya.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class WeekResponse(
    val id: Long,
    val bookId: Long,
    val number: Int,
    val title: String,
)
