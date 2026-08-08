package com.spasinnya.domain.model.homework

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LessonHomeworkStatus(
    @SerialName("lesson_number")
    val lessonNumber: Int,
    val completed: Long
)
