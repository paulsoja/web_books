package com.spasinnya.domain.model.book

import kotlinx.serialization.Serializable

@Serializable
data class LessonContent(
    val type: String, // "text" or "image"
    val data: String
)
