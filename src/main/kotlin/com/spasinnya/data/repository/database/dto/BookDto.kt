package com.spasinnya.data.repository.database.dto

import kotlinx.serialization.Serializable

@Serializable
data class BookDto(
    val id: Long,
    val number: String,
    val title: String,
    val subtitle: String? = null,
    val contents: ContentsDto
)
