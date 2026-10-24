package com.spasinnya

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.spasinnya.domain.repository.UserRepository
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity(users: UserRepository) {
    // Please read the jwt property from the config file if you are using EngineMain
    val jwtAudience = "jwt-audience"
    val jwtDomain = "https://jwt-provider-domain/"
    val issuer = System.getenv("JWT_ISSUER") ?: error("JWT_ISSUER is missing!")
    val jwtRealm = "ktor sample app"
    val jwtSecret = System.getenv("JWT_SECRET") ?: error("JWT_SECRET is missing!")
    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    //.withAudience(jwtAudience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.subject?.toLongOrNull()?.takeIf { it > 0 }
                if (userId != null && users.findById(userId).getOrThrow() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
