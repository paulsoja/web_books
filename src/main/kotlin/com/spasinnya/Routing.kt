package com.spasinnya

import com.spasinnya.domain.exception.BadRequestException
import com.spasinnya.domain.exception.ConflictException
import com.spasinnya.domain.exception.ForbiddenException
import com.spasinnya.domain.exception.InvalidOtpException
import com.spasinnya.domain.exception.NotFoundException
import com.spasinnya.domain.exception.TooManyRequestsException
import com.spasinnya.domain.exception.UnauthorizedException
import com.spasinnya.domain.exception.UnprocessableEntityException
import com.spasinnya.domain.exception.UserNotFoundException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<UnauthorizedException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to (cause.message ?: "Unauthorized")))
        }
        exception<ForbiddenException> { call, cause ->
            call.respond(HttpStatusCode.Forbidden, mapOf("error" to (cause.message ?: "Forbidden")))
        }
        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, mapOf("error" to (cause.message ?: "Not found")))
        }
        exception<BadRequestException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to (cause.message ?: "Bad request")))
        }
        exception<ConflictException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, mapOf("error" to (cause.message ?: "Conflict")))
        }
        exception<UnprocessableEntityException> { call, cause ->
            call.respond(HttpStatusCode.UnprocessableEntity, mapOf("error" to (cause.message ?: "Unprocessable entity")))
        }
        exception<TooManyRequestsException> { call, cause ->
            call.respond(HttpStatusCode.TooManyRequests, mapOf("error" to (cause.message ?: "Too many requests")))
        }
        exception<UserNotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, mapOf("error" to (cause.message ?: "User not found")))
        }
        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Something went wrong"))
        }
    }
}
