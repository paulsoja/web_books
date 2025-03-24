package com.spasinnya.data.repository.table

import com.spasinnya.domain.model.book.Book
import kotlinx.serialization.Serializable

@Serializable
data class BooksWrapper(
    val books: List<Book>
)
