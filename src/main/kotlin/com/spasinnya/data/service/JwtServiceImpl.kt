package com.spasinnya.data.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.spasinnya.data.extension.sha256
import com.spasinnya.domain.model.auth.IssuedTokens
import com.spasinnya.domain.port.TokenService
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class JwtServiceImpl : TokenService {

    private val secret = System.getenv("JWT_SECRET") ?: error("JWT_SECRET is missing!")
    private val issuer = System.getenv("JWT_ISSUER") ?: error("JWT_ISSUER is missing!")
    private val algorithm = Algorithm.HMAC256(secret)

    private val accessTtl = 3.days
    private val refreshTtl = 7.days

    override fun issueTokens(userId: Long, email: String): IssuedTokens {
        val now: Instant = Clock.System.now()
        val accessExp: Instant = now + accessTtl
        val refreshExp: Instant = now + refreshTtl

        val accessToken = JWT.create()
            .withIssuer(issuer)
            .withSubject(userId.toString())
            .withClaim("email", email)
            .withExpiresAt(Date(accessExp.toEpochMilliseconds()))
            .sign(algorithm)

        val refreshToken = JWT.create()
            .withIssuer(issuer)
            .withSubject(userId.toString())
            .withClaim("email", email)
            .withExpiresAt(Date(refreshExp.toEpochMilliseconds()))
            .sign(algorithm)

        return IssuedTokens(
            accessToken = accessToken,
            accessExpiresAt = accessExp,
            refreshToken = refreshToken,
            refreshTokenHash = sha256(refreshToken),
            refreshExpiresAt = refreshExp
        )
    }
}