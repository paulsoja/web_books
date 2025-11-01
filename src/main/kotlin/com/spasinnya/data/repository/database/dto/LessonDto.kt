package com.spasinnya.data.repository.database.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LessonDto(
    val number: Int,
    val title: String,
    val quote: String? = null,
    val content: List<ContentBlockDto>? = null,
    @SerialName("home_work") val homeWork: HomeworkDto? = null
)
