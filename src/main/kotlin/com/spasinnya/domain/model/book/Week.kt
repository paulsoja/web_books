package com.spasinnya.domain.model.book

import kotlinx.serialization.Serializable

@Serializable
data class Week(
    val id: Long,
    val bookId: Long,
    val number: Int,
    val title: String,
)
