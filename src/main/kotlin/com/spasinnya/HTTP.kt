package com.spasinnya

import com.spasinnya.domain.usecase.AuthUseCase
import com.spasinnya.domain.usecase.GetBookByIdUseCase
import com.spasinnya.domain.usecase.GetBooksUseCase
import com.spasinnya.presentation.routes.authRoutes
import com.spasinnya.presentation.routes.bookRoutes
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.swagger.codegen.v3.generators.html.StaticHtmlCodegen

fun Application.configureHTTP(
    authUseCase: AuthUseCase,
    getBooksUseCase: GetBooksUseCase,
    getBookByIdUseCase: GetBookByIdUseCase
) {
    routing {
        authRoutes(authUseCase)

        authenticate("auth-jwt") {
            bookRoutes(getBooksUseCase, getBookByIdUseCase)
        }
    }
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
    }
    routing {
        openAPI(path="openapi", swaggerFile = "openapi/documentation.yaml") {
            codegen = StaticHtmlCodegen()
        }
    }
}
