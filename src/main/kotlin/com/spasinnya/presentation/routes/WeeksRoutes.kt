package com.spasinnya.presentation.routes

import com.spasinnya.domain.usecase.GetWeeksUseCase
import com.spasinnya.presentation.mapper.toPresentation
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.weekRoutes(
    getWeeksUseCase: GetWeeksUseCase,
) {
    get("/weeks/{bookId}") {
        val bookIdParam = call.parameters["bookId"]

        if (bookIdParam.isNullOrBlank()) {
            return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing path parameter 'bookId'")
            )
        }

        val bookId = bookIdParam.toLongOrNull() ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Invalid 'bookId' format, must be a number")
        )

        val result = getWeeksUseCase.invoke(bookId)

        result.fold(
            onSuccess = { weeks -> call.respond(HttpStatusCode.OK, weeks.map { it.toPresentation() }) },
            onFailure = { call.respond(HttpStatusCode.NotFound, "Weeks not found") }
        )
    }
}