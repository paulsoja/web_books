@file:OptIn(ExperimentalTime::class)

package com.spasinnya.presentation.routes

import com.spasinnya.domain.usecase.*
import com.spasinnya.presentation.model.LoginRequest
import com.spasinnya.presentation.model.RegisterRequest
import com.spasinnya.presentation.model.TokenResponse
import com.spasinnya.presentation.model.VerifyOtpRequest
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.time.ExperimentalTime

fun Route.authRoutes(
    registerUser: RegisterUserUseCase,
    verifyOtp: VerifyOtpUseCase,
    loginUser: LoginUserUseCase,
    refreshSession: RefreshSessionUseCase,
    logout: LogoutUseCase
) {
    post("/register") {
        val req = call.receive<RegisterRequest>()
        val result = registerUser(req.email, req.password)
        result.onSuccess { call.respond(HttpStatusCode.Created) }
            .onFailure { call.respond(HttpStatusCode.BadRequest, it.message ?: "Error") }
    }

    post("/verify-otp") {
        val req = call.receive<VerifyOtpRequest>()
        val result = verifyOtp(req.email, req.code)
        result.onSuccess { call.respond(HttpStatusCode.OK) }
            .onFailure { call.respond(HttpStatusCode.BadRequest, it.message ?: "Error") }
    }

    post("/login") {
        val req = call.receive<LoginRequest>()
        val ua = call.request.headers["User-Agent"]
        val ip = call.request.origin.remoteHost

        val result = loginUser(req.email, req.password, ua, ip)
        result.onSuccess {
            call.respond(
                TokenResponse(
                    accessToken = it.accessToken,
                    accessExpiresAt = it.accessExpiresAt.toString(),
                    refreshToken = it.refreshToken,
                    refreshExpiresAt = it.refreshExpiresAt.toString()
                )
            )
        }.onFailure {
            call.respond(HttpStatusCode.Unauthorized, it.message ?: "Unauthorized")
        }
    }

    post("/refresh") {
        val token = call.request.queryParameters["refreshToken"] ?: return@post call.respond(HttpStatusCode.BadRequest)
        val result = refreshSession(token)
        result.onSuccess {
            call.respond(TokenResponse(
                accessToken = it.accessToken,
                accessExpiresAt = it.accessExpiresAt.toString(),
                refreshToken = it.refreshToken,
                refreshExpiresAt = it.refreshExpiresAt.toString()
            ))
        }.onFailure {
            call.respond(HttpStatusCode.Unauthorized, it.message ?: "Unauthorized")
        }
    }

    post("/logout") {
        val token = call.request.queryParameters["refreshToken"] ?: return@post call.respond(HttpStatusCode.BadRequest)
        val result = logout.revokeSingle(token)
        result.onSuccess { call.respond(HttpStatusCode.OK) }
            .onFailure { call.respond(HttpStatusCode.BadRequest, it.message ?: "Error") }
    }
}