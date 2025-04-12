package com.spasinnya.data.repository.database.mapper

import com.spasinnya.data.repository.database.dto.BookFullDto
import com.spasinnya.domain.model.book.Author
import com.spasinnya.domain.model.book.Book
import com.spasinnya.domain.model.book.BookContent

fun BookFullDto.toBook(): Book = Book(
    id = id,
    number = number,
    title = title,
    subtitle = subtitle,
    contents = BookContent(
        weeks = weeks.map { it.toWeek() },
        author = Author(
            by_author = "",
            how_to_use = "",
            before_starting = ""
        )
    )
)