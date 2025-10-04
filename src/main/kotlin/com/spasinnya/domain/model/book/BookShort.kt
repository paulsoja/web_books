package com.spasinnya.domain.model.book

data class BookShort(
    val id: Long,
    val number: String,
    val title: String,
    val subtitle: String? = null,
    val isPurchased: Boolean,
)
