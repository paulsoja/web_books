package com.spasinnya.presentation.routes

import com.spasinnya.domain.usecase.GetUserProfileUseCase
import com.spasinnya.domain.usecase.DeleteAccountUseCase
import com.spasinnya.domain.usecase.UpdateUserProfileUseCase
import com.spasinnya.presentation.mapper.toResponse
import com.spasinnya.presentation.model.UpdateUserProfileRequest
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(
    getUserProfileUseCase: GetUserProfileUseCase,
    updateUserProfileUseCase: UpdateUserProfileUseCase,
    deleteAccountUseCase: DeleteAccountUseCase
) {
    delete("/me") {
        val userId = call.principal<JWTPrincipal>()?.payload?.subject?.toLongOrNull()
            ?.takeIf { it > 0 }
            ?: return@delete call.respond(HttpStatusCode.Unauthorized)

        deleteAccountUseCase(userId).getOrThrow()
        call.respond(HttpStatusCode.NoContent)
    }

    get("/me") {
        val principal = call.principal<JWTPrincipal>()
            ?: return@get call.respond(HttpStatusCode.Unauthorized)

        val userId = principal.payload.getClaim("sub").asString().toLongOrNull()
            ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))

        val result = getUserProfileUseCase.invoke(userId)
        result.fold(
            onSuccess = { userProfile -> call.respond(HttpStatusCode.OK, userProfile.toResponse()) },
            onFailure = { call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found")) }
        )
    }

    patch("/me") {
        val principal = call.principal<JWTPrincipal>()
            ?: return@patch call.respond(HttpStatusCode.Unauthorized)

        val userId = principal.payload.getClaim("sub").asString().toLongOrNull()
            ?: return@patch call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))

        val req = call.receive<UpdateUserProfileRequest>()

        val result = updateUserProfileUseCase(userId, req.firstName, req.lastName)

        result.fold(
            onSuccess = { userProfile -> call.respond(HttpStatusCode.OK, userProfile.toResponse()) },
            onFailure = { call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found")) }
        )
    }
}
