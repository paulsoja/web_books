package com.spasinnya.data.repository.database.table

import com.spasinnya.domain.model.book.Book
import kotlinx.serialization.Serializable

@Serializable
data class BooksWrapper(
    val books: List<Book>
)
