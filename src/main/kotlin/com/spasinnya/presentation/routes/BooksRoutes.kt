package com.spasinnya.presentation.routes

import com.spasinnya.domain.usecase.GetBooksUseCase
import com.spasinnya.domain.usecase.PurchaseBookSimpleUseCase
import com.spasinnya.presentation.mapper.toPresentation
import com.spasinnya.presentation.model.PurchaseResponse
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.bookRoutes(
    getBooksUseCase: GetBooksUseCase,
    purchaseBookSimpleUseCase: PurchaseBookSimpleUseCase
) {
    get("/books") {
        val principal = call.principal<JWTPrincipal>()
            ?: return@get call.respond(HttpStatusCode.Unauthorized)

        val userId = principal.payload.getClaim("sub").asString().toLongOrNull()
            ?: return@get call.respond(HttpStatusCode.Unauthorized, "Invalid token")

        val result = getBooksUseCase.invoke(userId)

        result.fold(
            onSuccess = { books -> call.respond(HttpStatusCode.OK, books.map { it.toPresentation() }) },
            onFailure = { call.respond(HttpStatusCode.NotFound, "Books not found") }
        )
    }

    post("/books/{id}/purchase") {
        val principal = call.principal<JWTPrincipal>()
            ?: return@post call.respond(HttpStatusCode.Unauthorized)

        val userId = principal.payload.getClaim("sub").asString().toLongOrNull()
            ?: return@post call.respond(HttpStatusCode.Unauthorized, "Invalid token")

        val bookId = call.parameters["id"]?.toLongOrNull()
            ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid book id")

        val result = purchaseBookSimpleUseCase(userId, bookId)
        result.fold(
            onSuccess = {
                call.respond(HttpStatusCode.OK, PurchaseResponse(bookId = bookId, purchased = true))
            },
            onFailure = { e ->
                when (e) {
                    is IllegalArgumentException -> {
                        // из use case: require(exists) -> "Book not found"
                        if (e.message?.contains("Book not found") == true)
                            call.respond(HttpStatusCode.NotFound, "Book not found")
                        else
                            call.respond(HttpStatusCode.BadRequest, e.message ?: "Bad request")
                    }
                    else -> {
                        e.printStackTrace()
                        call.respond(HttpStatusCode.InternalServerError, "Internal server error")
                    }
                }
            }
        )
    }
}