package com.spasinnya.domain.repository

import com.spasinnya.domain.model.book.Book
import com.spasinnya.domain.model.book.BookShort

interface BookRepository {
    suspend fun getAllBooksWithContentByLanguage(language: String): Result<List<Book>>
    suspend fun getBooksByLanguage(language: String): Result<List<BookShort>>
    suspend fun getBookById(bookId: Int): Result<Book>
    suspend fun exists(bookId: Long): Result<Boolean>
}