package com.spasinnya.presentation.routes

import com.spasinnya.domain.repository.BookRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.bookRoutes(
    bookRepository: BookRepository
) {
    get("/books") {
        val principal = call.principal<JWTPrincipal>()
            ?: return@get call.respond(HttpStatusCode.Unauthorized)

        val books = bookRepository.getAllBooks()

        call.respond(books)
    }
}