package com.spasinnya.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class LessonResponse(
    val number: Int,
    val title: String,
    val quote: String? = null,
    val content: List<LessonContentResponse>
)
