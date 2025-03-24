package com.spasinnya.data.repository

import com.spasinnya.data.repository.table.Books
import com.spasinnya.domain.model.book.Author
import com.spasinnya.domain.model.book.Book
import com.spasinnya.domain.model.book.BookContent
import com.spasinnya.domain.repository.BookRepository
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class BookRepositoryImpl : BookRepository {
    override fun getAllBooks(): List<Book> = transaction {
        Books.selectAll().map {
            Book(
                id = it[Books.id].value,
                title = it[Books.title],
                number = it[Books.number],
                subtitle = it[Books.subtitle],
                contents = BookContent(
                    weeks = emptyList(),
                    author = Author(
                        "", "", ""
                    )
                )
            )
        }
    }
}