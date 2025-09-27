package com.spasinnya.presentation.routes

import com.spasinnya.domain.usecase.GetUserProfileUseCase
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(
    getUserProfileUseCase: GetUserProfileUseCase
) {
    get("/me") {
        val principal = call.principal<JWTPrincipal>()
            ?: return@get call.respond(HttpStatusCode.Unauthorized)

        val userId = principal.payload.getClaim("email").asString().toLongOrNull()
            ?: return@get call.respond(HttpStatusCode.Unauthorized, "Invalid token")

        val result = getUserProfileUseCase.invoke(userId)
        result.fold(
            onSuccess = { call.respond(result) },
            onFailure = { call.respond(HttpStatusCode.NotFound, "User not found") }
        )
    }
}