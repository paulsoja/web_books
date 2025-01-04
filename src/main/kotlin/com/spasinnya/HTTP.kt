package com.spasinnya

import com.spasinnya.domain.usecase.AuthUseCase
import com.spasinnya.presentation.routes.authRoutes
import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.swagger.codegen.v3.generators.html.StaticHtmlCodegen

fun Application.configureHTTP(authUseCase: AuthUseCase) {
    routing {
        authRoutes(authUseCase)
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
