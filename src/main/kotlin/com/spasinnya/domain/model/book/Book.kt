package com.spasinnya.domain.model.book

import kotlinx.serialization.Serializable

@Serializable
data class Book(
    val id: Long,
    val number: String,
    val title: String,
    val subtitle: String? = null,
    val language: String,
    val contents: BookContent,
)
