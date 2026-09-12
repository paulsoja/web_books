package com.spasinnya

import com.spasinnya.domain.exception.*
import com.spasinnya.domain.model.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<PurchaseException> { call, cause ->
            val status = when (cause.error) {
                PurchaseError.TRANSACTION_ALREADY_CLAIMED -> HttpStatusCode.Conflict
                PurchaseError.VERIFICATION_UNAVAILABLE -> HttpStatusCode.ServiceUnavailable
                PurchaseError.UNKNOWN_PRODUCT -> HttpStatusCode.UnprocessableEntity
                else -> HttpStatusCode.BadRequest
            }
            call.respond(status, ErrorResponse(reason = cause.error.name, statusCode = status.description))
        }
        exception<UnauthorizedException> { call, cause ->
            call.respond(
                status = HttpStatusCode.Unauthorized,
                message = ErrorResponse(
                    reason = cause.message ?: "Unauthorized",
                    statusCode = HttpStatusCode.Unauthorized.description
                )
            )
        }
        exception<ForbiddenException> { call, cause ->
            call.respond(
                status = HttpStatusCode.Forbidden,
                message = ErrorResponse(
                    reason = cause.message ?: "Forbidden",
                    statusCode = HttpStatusCode.Forbidden.description
                )
            )
        }
        exception<NotFoundException> { call, cause ->
            call.respond(
                status = HttpStatusCode.NotFound,
                message = ErrorResponse(
                    reason = cause.message ?: "Not found",
                    statusCode = HttpStatusCode.NotFound.description
                )
            )
        }
        exception<BadRequestException> { call, cause ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse(
                    reason = cause.message ?: "Bad request",
                    statusCode = HttpStatusCode.BadRequest.description
                )
            )
        }
        exception<ConflictException> { call, cause ->
            call.respond(
                status = HttpStatusCode.Conflict,
                message = ErrorResponse(
                    reason = cause.message ?: "Conflict",
                    statusCode = HttpStatusCode.Conflict.description
                )
            )
        }
        exception<UnprocessableEntityException> { call, cause ->
            call.respond(
                status = HttpStatusCode.UnprocessableEntity,
                message = ErrorResponse(
                    reason = cause.message ?: "Unprocessable entity",
                    statusCode = HttpStatusCode.UnprocessableEntity.description
                )
            )
        }
        exception<TooManyRequestsException> { call, cause ->
            call.respond(
                status = HttpStatusCode.TooManyRequests,
                message = ErrorResponse(
                    reason = cause.message ?: "Too many requests",
                    statusCode = HttpStatusCode.TooManyRequests.description
                )
            )
        }
        exception<UserNotFoundException> { call, cause ->
            call.respond(
                status = HttpStatusCode.NotFound,
                message = ErrorResponse(
                    reason = cause.message ?: "User not found",
                    statusCode = HttpStatusCode.NotFound.description
                )
            )
        }
        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse(
                    reason = "Something went wrong",
                    statusCode = HttpStatusCode.InternalServerError.description
                )
            )
        }
    }
}
