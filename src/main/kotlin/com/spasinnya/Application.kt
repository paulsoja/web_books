package com.spasinnya

import com.spasinnya.data.repository.DatabaseUserRepository
import com.spasinnya.data.service.JwtServiceImpl
import com.spasinnya.data.service.OtpServiceImpl
import com.spasinnya.domain.repository.JwtService
import com.spasinnya.domain.repository.OtpService
import com.spasinnya.domain.usecase.AuthUseCase
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val userRepository = DatabaseUserRepository()
    val otpService: OtpService = OtpServiceImpl(EmailServiceSingleton.instance)
    val jwtService: JwtService = JwtServiceImpl()
    val authUseCase = AuthUseCase(userRepository, otpService, jwtService)

    configureSerialization()
    DatabaseFactory.init()
    configureMonitoring()
    configureHTTP(authUseCase)
    configureSecurity()
    configureRouting()
}
