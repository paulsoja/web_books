package com.spasinnya.presentation.routes

import com.spasinnya.domain.usecase.GetBooksUseCase
import com.spasinnya.presentation.mapper.toPresentation
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.bookRoutes(
    getBooksUseCase: GetBooksUseCase
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
}