package com.spasinnya.presentation.routes

import com.spasinnya.domain.usecase.GetLessonsByWeekIdUseCase
import com.spasinnya.presentation.mapper.toPresentation
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.lessonsRoutes(
    getLessonsByWeekIdUseCase: GetLessonsByWeekIdUseCase
) {
    get("/lessons/{weekId}") {
        val weekIdParam = call.parameters["weekId"]

        if (weekIdParam.isNullOrBlank()) {
            return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing path parameter 'weekId'")
            )
        }

        val weekId = weekIdParam.toLongOrNull() ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to "Invalid 'weekId' format, must be a number")
        )

        val result = getLessonsByWeekIdUseCase.invoke(weekId)
        result.fold(
            onSuccess = { lessons -> call.respond(HttpStatusCode.OK, lessons.map { it.toPresentation() }) },
            onFailure = { call.respond(HttpStatusCode.NotFound, "Lessons not found") }
        )
    }
}