package com.spasinnya.presentation.routes

import com.spasinnya.domain.exception.UnauthorizedException
import com.spasinnya.domain.exception.UserNotFoundException
import com.spasinnya.domain.usecase.GetUserProfileUseCase
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(
    getUserProfileUseCase: GetUserProfileUseCase,
) {
    get("/profile") {
        val principal = call.principal<JWTPrincipal>() ?: throw UnauthorizedException()
        val email = principal.payload.getClaim("email").asString()

        getUserProfileUseCase.invoke(email)
            .getOrThrow()
            .let { call.respond(HttpStatusCode.OK, it) }
    }
}