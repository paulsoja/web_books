package com.spasinnya.domain.model.book

import kotlinx.serialization.Serializable

@Serializable
data class BookContent(
    val weeks: List<Week>,
    val author: Author
)
