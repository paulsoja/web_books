package com.spasinnya.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class HomeworkValueDto(
    val id: String,
    val answer: String
)
