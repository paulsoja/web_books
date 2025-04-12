package com.spasinnya

import com.spasinnya.DatabaseFactory.loadBooksFromJson
import com.spasinnya.data.repository.BookRepositoryImpl
import com.spasinnya.data.repository.database.DatabaseUserRepository
import com.spasinnya.data.service.JwtServiceImpl
import com.spasinnya.data.service.OtpServiceImpl
import com.spasinnya.domain.repository.BookRepository
import com.spasinnya.domain.repository.JwtService
import com.spasinnya.domain.repository.OtpService
import com.spasinnya.domain.usecase.AuthUseCase
import com.spasinnya.domain.usecase.GetBookByIdUseCase
import com.spasinnya.domain.usecase.GetBooksUseCase
import com.spasinnya.domain.usecase.GetUserProfileUseCase
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val userRepository = DatabaseUserRepository()
    val booksRepository: BookRepository = BookRepositoryImpl()
    val otpService: OtpService = OtpServiceImpl(EmailServiceSingleton.instance)
    val jwtService: JwtService = JwtServiceImpl()
    val authUseCase = AuthUseCase(userRepository, otpService, jwtService)
    val booksUseCase = GetBooksUseCase(bookRepository = booksRepository)
    val bookByIdUseCase = GetBookByIdUseCase(bookRepository = booksRepository)
    val userProfileUseCase = GetUserProfileUseCase(userRepository = userRepository)

    configureSerialization()

    DatabaseFactory.init().also {
        val content = loadBooksFromJson()
        DatabaseFactory.seedDatabase(content.books)
    }

    configureMonitoring()
    configureSecurity()
    configureHTTP(authUseCase, booksUseCase, bookByIdUseCase, userProfileUseCase)
    configureRouting()
}
