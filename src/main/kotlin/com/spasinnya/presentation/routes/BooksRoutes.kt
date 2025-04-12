package com.spasinnya.presentation.routes

import com.spasinnya.domain.model.book.Book
import com.spasinnya.domain.usecase.GetBookByIdUseCase
import com.spasinnya.domain.usecase.GetBooksUseCase
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.bookRoutes(
    getBooksUseCase: GetBooksUseCase,
    getBookByIdUseCase: GetBookByIdUseCase
) {
    get("/books") {
        try {
            val books: List<Book> = getBooksUseCase.execute()
            call.respond(HttpStatusCode.OK, books)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error fetching books: ${e.message}")
        }
    }

    get("book/{id}") {
        val bookId = call.parameters["id"]?.toIntOrNull()
        if (bookId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid book ID")
            return@get
        }

        val book = getBookByIdUseCase.execute(bookId)
        if (book == null) {
            call.respond(HttpStatusCode.NotFound, "Book not found")
        } else {
            call.respond(book)
        }
    }
}