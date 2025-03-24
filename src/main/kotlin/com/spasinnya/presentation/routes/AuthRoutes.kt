package com.spasinnya.presentation.routes

import com.spasinnya.domain.exception.InvalidOtpException
import com.spasinnya.domain.exception.UserNotFoundException
import com.spasinnya.domain.model.auth.*
import com.spasinnya.domain.usecase.AuthUseCase
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authUseCase: AuthUseCase) {
    post("/register") {
        val request = call.receive<RegisterRequest>()
        authUseCase.register(request.email, request.password)
        call.respondText("OTP sent to ${request.email}")
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
        val request = call.receive<LoginRequest>()
        val tokens = authUseCase.login(request.email, request.password)
        call.respond(tokens)
    }

    post("/reset-password") {
        val request = call.receive<ResetPasswordRequest>()
        authUseCase.sendResetPasswordOtp(request.email)
        call.respondText("OTP sent to ${request.email}")
    }

    post("/set-new-password") {
        val request = call.receive<SetNewPasswordRequest>()
        authUseCase.resetPassword(request.email, request.otpCode, request.newPassword)
        call.respondText("Password updated successfully")
    }
}