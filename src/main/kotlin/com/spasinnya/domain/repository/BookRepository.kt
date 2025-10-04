package com.spasinnya.domain.repository

import com.spasinnya.domain.model.book.Book
import com.spasinnya.domain.model.book.BookShort

interface BookRepository {
    suspend fun getAllBooksWithContent(): Result<List<Book>>
    suspend fun getBooks(): Result<List<BookShort>>
    suspend fun getBookById(bookId: Int): Result<Book>
}