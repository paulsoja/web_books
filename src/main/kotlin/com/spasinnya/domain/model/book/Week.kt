package com.spasinnya.domain.model.book

import kotlinx.serialization.Serializable

@Serializable
data class Week(
    val number: Int,
    val title: String,
    val lessons: List<Lesson>
)
