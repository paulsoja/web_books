package com.spasinnya.domain.repository

import com.spasinnya.domain.model.book.Book

interface BookRepository {
    fun getAllBooks(): List<Book>
    fun getBookById(bookId: Int): Book
}