package com.spasinnya.domain.model.homework

import kotlinx.serialization.Serializable

@Serializable
data class BookHomeworkStatus(
    val bookId: String,
    val completedLessons: Int
)
