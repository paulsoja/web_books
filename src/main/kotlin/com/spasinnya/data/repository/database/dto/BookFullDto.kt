package com.spasinnya.data.repository.database.dto

import kotlinx.serialization.Serializable

@Serializable
data class LessonDto(
    val id: Int,
    val number: Int,
    val title: String,
    val quote: String? = null,
)

@Serializable
data class WeekDto(
    val id: Int,
    val number: Int,
    val title: String,
    val lessons: List<LessonDto>
)

@Serializable
data class BookFullDto(
    val id: Int,
    val number: String,
    val title: String,
    val subtitle: String? = null,
    val weeks: List<WeekDto>
)
