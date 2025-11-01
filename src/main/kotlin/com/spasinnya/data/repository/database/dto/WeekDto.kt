package com.spasinnya.data.repository.database.dto

import kotlinx.serialization.Serializable

@Serializable
data class WeekDto(
    val number: Int,
    val title: String,
    val lessons: List<LessonDto>
)
