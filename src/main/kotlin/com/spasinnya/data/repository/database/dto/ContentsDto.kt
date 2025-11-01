package com.spasinnya.data.repository.database.dto

import kotlinx.serialization.Serializable

@Serializable
data class ContentsDto(
    val weeks: List<WeekDto>
)
