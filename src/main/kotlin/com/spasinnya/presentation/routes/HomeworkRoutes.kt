package com.spasinnya.presentation.routes

import com.spasinnya.domain.usecase.GetHomeworkAnswersUseCase
import com.spasinnya.domain.usecase.GetLessonsWithAnswersUseCase
import com.spasinnya.domain.usecase.SaveHomeworkAnswersUseCase
import com.spasinnya.presentation.mapper.toDomain
import com.spasinnya.presentation.mapper.toHomeworkResponse
import com.spasinnya.presentation.model.HomeworkRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.principal
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put

fun Route.homeworkRoutes(
    saveHomeworkAnswersUseCase: SaveHomeworkAnswersUseCase,
    getHomeworkAnswersUseCase: GetHomeworkAnswersUseCase,
    getLessonsWithAnswersUseCase: GetLessonsWithAnswersUseCase
) {
    val basePath = "/homework/{bookId}/weeks/{weekNumber}/lessons/{lessonNumber}"
    val lessonsListPath = "/homework/{bookId}/weeks/{weekNumber}/lessons"

    get(lessonsListPath) {
        val userId = call.userIdOrNull()
            ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))

        val bookId = call.parameters["bookId"]?.takeIf { it.isNotBlank() }
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing path parameter 'bookId'"))

        val weekNumber = call.parameters["weekNumber"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid 'weekNumber'"))

        val result = getLessonsWithAnswersUseCase.invoke(
            userId = userId,
            bookId = bookId,
            weekNumber = weekNumber
        )

        result.fold(
            onSuccess = { lessons -> call.respond(HttpStatusCode.OK, lessons) },
            onFailure = { call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to load lessons")) }
        )
    }

    put(basePath) {
        val userId = call.userIdOrNull()
            ?: return@put call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))

        val bookId = call.parameters["bookId"]?.takeIf { it.isNotBlank() }
            ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing path parameter 'bookId'"))

        val weekNumber = call.parameters["weekNumber"]?.toIntOrNull()
            ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid 'weekNumber'"))

        val lessonNumber = call.parameters["lessonNumber"]?.toIntOrNull()
            ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid 'lessonNumber'"))

        val body = call.receive<HomeworkRequest>()

        val result = saveHomeworkAnswersUseCase.invoke(
            userId = userId,
            bookId = bookId,
            weekNumber = weekNumber,
            lessonNumber = lessonNumber,
            answers = body.toDomain()
        )

        result.fold(
            onSuccess = { call.respond(HttpStatusCode.NoContent) },
            onFailure = { call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to save answers")) }
        )
    }

    get(basePath) {
        val userId = call.userIdOrNull()
            ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))

        val bookId = call.parameters["bookId"]?.takeIf { it.isNotBlank() }
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing path parameter 'bookId'"))

        val weekNumber = call.parameters["weekNumber"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid 'weekNumber'"))

        val lessonNumber = call.parameters["lessonNumber"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid 'lessonNumber'"))

        val result = getHomeworkAnswersUseCase.invoke(
            userId = userId,
            bookId = bookId,
            weekNumber = weekNumber,
            lessonNumber = lessonNumber
        )

        result.fold(
            onSuccess = { answers -> call.respond(HttpStatusCode.OK, answers.toHomeworkResponse()) },
            onFailure = { call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to load answers")) }
        )
    }
}

private fun io.ktor.server.application.ApplicationCall.userIdOrNull(): Long? =
    principal<JWTPrincipal>()?.payload?.getClaim("sub")?.asString()?.toLongOrNull()
