package com.spasinnya.data.repository

import com.spasinnya.data.extension.runDb
import com.spasinnya.data.repository.database.table.Books
import com.spasinnya.domain.model.book.Author
import com.spasinnya.domain.model.book.Book
import com.spasinnya.domain.model.book.BookContent
import com.spasinnya.domain.model.book.BookShort
import com.spasinnya.domain.repository.BookRepository
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll

class BookDataRepository(
    private val database: Database
) : BookRepository {

    override suspend fun getAllBooksWithContent(): Result<List<Book>> = database.runDb {
        Books.selectAll().map {
            Book(
                id = it[Books.id],
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

    override suspend fun getBooks(): Result<List<BookShort>> = database.runDb {
        Books.selectAll().map {
            BookShort(
                id = it[Books.id],
                title = it[Books.title],
                number = it[Books.number],
                subtitle = it[Books.subtitle],
                isPurchased = false
            )
        }
    }

    override suspend fun getBookById(bookId: Int): Result<Book> = throw NotImplementedError()

    override suspend fun exists(bookId: Long): Result<Boolean> = database.runDb {
        Books
            .selectAll()
            .where { Books.id eq bookId }
            .limit(1)
            .any()
    }
}