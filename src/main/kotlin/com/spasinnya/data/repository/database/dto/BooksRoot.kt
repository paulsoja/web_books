package com.spasinnya.data.repository.database.dto

import kotlinx.serialization.Serializable

@Serializable
data class BooksRoot(
    val books: List<BookDto>
)
