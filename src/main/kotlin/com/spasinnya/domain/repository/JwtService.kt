package com.spasinnya.domain.repository

import com.spasinnya.domain.model.User

interface JwtService {
    fun generateAccessToken(user: User): String
    fun generateRefreshToken(user: User): String
}