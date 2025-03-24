package com.spasinnya.domain.model.book

import kotlinx.serialization.Serializable

@Serializable
data class Author(
    val by_author: String,
    val how_to_use: String,
    val before_starting: String
)
