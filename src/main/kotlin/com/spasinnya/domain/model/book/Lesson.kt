package com.spasinnya.domain.model.book

import kotlinx.serialization.Serializable

@Serializable
data class Lesson(
    val number: Int,
    val title: String,
    val quote: String? = null,
    val content: List<LessonContent>
)
