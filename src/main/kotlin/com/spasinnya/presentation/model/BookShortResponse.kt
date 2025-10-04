package com.spasinnya.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class BookShortResponse(
    val id: Long,
    val number: String,
    val title: String,
    val subtitle: String? = null,
    val isPurchased: Boolean,
)
