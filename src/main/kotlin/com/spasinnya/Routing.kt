package com.spasinnya

import com.spasinnya.domain.exception.InvalidOtpException
import com.spasinnya.domain.exception.UnauthorizedException
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

        exception<UserNotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, mapOf("error" to (cause.message ?: "User not found")))
        }

        exception<InvalidOtpException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, mapOf("error" to (cause.message ?: "Invalid OTP for user")))
        }

        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Something went wrong"))
        }
    }
}
