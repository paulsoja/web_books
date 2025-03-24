package com.spasinnya.domain.repository

interface OtpService {
    suspend fun sendOtp(email: String, otpCode: String)
    fun verifyOtp(email: String, otpCode: String): Boolean
}