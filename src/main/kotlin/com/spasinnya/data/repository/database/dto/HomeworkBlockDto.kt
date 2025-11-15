package com.spasinnya.data.repository.database.dto

import kotlinx.serialization.Serializable

@Serializable
data class HomeworkBlockDto(
    val id: Int? = null,
    val component: String,
    val answer: String? = null,
    val text: String? = null
)
