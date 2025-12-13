@file:OptIn(ExperimentalTime::class)

package com.spasinnya.presentation.routes

import com.spasinnya.domain.usecase.*
import com.spasinnya.presentation.model.*
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun Route.authRoutes(
    registerUser: RegisterUserUseCase,
    requestOtpUseCase: RequestOtpUseCase,
    verifyOtp: VerifyEmailOtpUseCase,
    loginUser: LoginUserUseCase,
    refreshSession: RefreshSessionUseCase,
    logout: LogoutUseCase,
    clock: () -> Instant
) {
    post("/register") {
        val req = call.receive<RegisterRequest>()
        val result = registerUser(req.email, req.password)
        result.fold(
            onSuccess = { call.respond(HttpStatusCode.Created) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Error") }
        )
    }
    post("/request-otp") {
        val body = call.receive<RequestOtpBody>()

        val res = requestOtpUseCase(
            email = body.email,
            userAgent = call.request.headers["User-Agent"],
            ip = call.request.origin.remoteHost
        )

        if (res.isSuccess) {
            // обычно для OTP возвращают NoContent
            call.respond(HttpStatusCode.NoContent)
        } else {
            val msg = res.exceptionOrNull()?.message ?: "error"

            // Если хочешь “не палить существование email” — ВСЕГДА отвечай 204.
            // Но если пока не важно — можно так:
            val status = when (msg) {
                "Too many requests" -> HttpStatusCode.TooManyRequests
                "Invalid email" -> HttpStatusCode.BadRequest
                else -> HttpStatusCode.BadRequest
            }

            call.respond(status, mapOf("error" to msg))
        }
    }

    post("/verify-otp") {
        val req = call.receive<VerifyOtpRequest>()
        val result = verifyOtp(req.email, req.code)
        result.fold(
            onSuccess = {
                call.respond(
                    TokenResponse(
                        accessToken = it.accessToken,
                        accessExpiresAt = it.accessExpiresAt.toString(),
                        refreshToken = it.refreshToken,
                        refreshExpiresAt = it.refreshExpiresAt.toString()
                    )
                )
            },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Error") }
        )
    }

    post("/login") {
        val req = call.receive<LoginRequest>()
        val ua = call.request.headers["User-Agent"]
        val ip = call.request.origin.remoteHost

        val result = loginUser(req.email, req.password, ua, ip)
        result.fold(
            onSuccess = {
                call.respond(
                    TokenResponse(
                        accessToken = it.accessToken,
                        accessExpiresAt = it.accessExpiresAt.toString(),
                        refreshToken = it.refreshToken,
                        refreshExpiresAt = it.refreshExpiresAt.toString()
                    )
                )
            },
            onFailure = { call.respond(HttpStatusCode.Unauthorized, it.message ?: "Unauthorized") }
        )
    }

    post("/refresh") {
        val req = call.receive<RefreshRequest>()
        val result = refreshSession(req.refreshToken)
        result.fold(
            onSuccess = {
                call.respond(TokenResponse(
                    accessToken = it.accessToken,
                    accessExpiresAt = it.accessExpiresAt.toString(),
                    refreshToken = it.refreshToken,
                    refreshExpiresAt = it.refreshExpiresAt.toString()
                ))
            },
            onFailure = { call.respond(HttpStatusCode.Unauthorized, it.message ?: "Unauthorized") }
        )
    }

    post("/logout") {
        val token = call.receive<RefreshRequest>().refreshToken
        val result = logout.revokeSingleRaw(token)
        result.fold(
            onSuccess = { call.respond(HttpStatusCode.OK) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Error") }
        )
    }
}