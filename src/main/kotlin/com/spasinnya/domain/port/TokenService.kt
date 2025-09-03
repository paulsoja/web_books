package com.spasinnya.domain.port

import com.spasinnya.domain.model.auth.IssuedTokens

interface TokenService {
    fun issueTokens(userId: Long, email: String): IssuedTokens
}