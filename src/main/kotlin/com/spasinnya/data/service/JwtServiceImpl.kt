package com.spasinnya.data.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.spasinnya.domain.model.User
import com.spasinnya.domain.repository.JwtService
import java.util.*

class JwtServiceImpl : JwtService {

    private val secret = "your_secret_key"
    private val issuer = "your_app"
    private val algorithm = Algorithm.HMAC256(secret)

    override fun generateAccessToken(user: User): String {
        return JWT.create()
            .withIssuer(issuer)
            .withClaim("email", user.email)
            .withExpiresAt(Date(System.currentTimeMillis() + 3600 * 1000))
            .sign(algorithm)
    }

    override fun generateRefreshToken(user: User): String {
        return JWT.create()
            .withIssuer(issuer)
            .withClaim("email", user.email)
            .withExpiresAt(Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000))
            .sign(algorithm)
    }
}