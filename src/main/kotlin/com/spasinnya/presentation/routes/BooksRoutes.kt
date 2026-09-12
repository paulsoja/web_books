package com.spasinnya.presentation.routes

import com.spasinnya.domain.usecase.GetBooksUseCase
import com.spasinnya.presentation.helper.language
import com.spasinnya.presentation.mapper.toPresentation
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.bookRoutes(getBooksUseCase: GetBooksUseCase) {
    get("/books") {
        val principal = call.principal<JWTPrincipal>()
            ?: return@get call.respond(HttpStatusCode.Unauthorized)
        val userId = principal.payload.getClaim("sub").asString().toLongOrNull()
            ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
        val books = getBooksUseCase.invoke(userId, call.language()).getOrThrow()
        call.respond(HttpStatusCode.OK, books.map { it.toPresentation() })
    }
}
