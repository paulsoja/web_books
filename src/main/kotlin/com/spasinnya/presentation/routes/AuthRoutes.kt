package com.spasinnya.presentation.routes

import com.spasinnya.domain.exception.InvalidOtpException
import com.spasinnya.domain.exception.UserNotFoundException
import com.spasinnya.domain.model.auth.*
import com.spasinnya.domain.usecase.AuthUseCase
import io.ktor.client.request.request
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authUseCase: AuthUseCase) {
    post("/register") {
        call.receive<RegisterRequest>()
            .let { request ->
                authUseCase.register(request.email, request.password)
                call.respondText("OTP sent to ${request.email}")
            }
    }

    post("/verify-otp") {
        val request = call.receive<VerifyOtpRequest>()

        try {
            val response = authUseCase.verifyOtp(request.email, request.otpCode)
            call.respond(HttpStatusCode.OK, response)
        } catch (e: UserNotFoundException) {
            call.respond(HttpStatusCode.NotFound, "User not found")
        } catch (e: InvalidOtpException) {
            call.respond(HttpStatusCode.BadRequest, "Invalid OTP")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "An error occurred")
        }
    }

    post("/login") {
        call.receive<LoginRequest>()
            .let { request -> authUseCase.login(request.email, request.password) }
            .let { token -> call.respond(token) }
    }

    post("/reset-password") {
        call.receive<ResetPasswordRequest>()
            .let { request ->
                authUseCase.sendResetPasswordOtp(request.email)
                    .also { call.respondText("OTP sent to ${request.email}") }
            }
    }

    post("/set-new-password") {
        call.receive<SetNewPasswordRequest>()
            .let { request ->
                authUseCase.resetPassword(
                    email = request.email,
                    otpCode = request.otpCode,
                    newPassword = request.newPassword
                )
                call.respondText("Password updated successfully")
            }
    }
}