package com.spasinnya.data.repository.database.dto

import kotlinx.serialization.Serializable

@Serializable
data class HomeworkDto(
    val id: Int? = null,
    val question: String? = null,
    val block: List<HomeworkBlockDto>? = null
)
