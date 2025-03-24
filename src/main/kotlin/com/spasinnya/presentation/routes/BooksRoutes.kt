package com.spasinnya.presentation.routes

import com.spasinnya.domain.model.book.Book
import com.spasinnya.domain.usecase.GetBooksUseCase
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.bookRoutes(getBooksUseCase: GetBooksUseCase) {

        get("/books") {
            try {
                val books: List<Book> = getBooksUseCase.execute()
                call.respond(HttpStatusCode.OK, books)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error fetching books: ${e.message}")
            }
        }

}