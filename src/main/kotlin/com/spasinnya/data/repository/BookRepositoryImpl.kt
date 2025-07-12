package com.spasinnya.data.repository

import com.spasinnya.data.repository.database.dto.BookFullDto
import com.spasinnya.data.repository.database.dto.LessonDto
import com.spasinnya.data.repository.database.dto.WeekDto
import com.spasinnya.data.repository.database.entity.BookEntity
import com.spasinnya.data.repository.database.mapper.toBook
import com.spasinnya.data.repository.database.table.Books
import com.spasinnya.domain.model.book.Author
import com.spasinnya.domain.model.book.Book
import com.spasinnya.domain.model.book.BookContent
import com.spasinnya.domain.repository.BookRepository
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

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

    override fun getBookById(bookId: Int): Book? = transaction {
        BookEntity.findById(bookId)?.let { book ->
            BookFullDto(
                id = book.id.value,
                number = book.number,
                title = book.title,
                weeks = book.weeks.map { week ->
                    WeekDto(
                        id = week.id.value,
                        number = week.number,
                        title = week.title,
                        lessons = week.lessons.map { lesson ->
                            LessonDto(
                                id = lesson.id.value,
                                number = lesson.number,
                                title = lesson.title,
                                quote = lesson.quote
                            )
                        }
                    )
                }
            ).toBook()
        }
    }
}