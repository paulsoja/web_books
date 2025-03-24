package com.spasinnya.data.service

import com.spasinnya.domain.repository.OtpService

class OtpServiceImpl(
    private val emailService: EmailService,
) : OtpService {

    private val otpStorage = mutableMapOf<String, String>()

    override suspend fun sendOtp(email: String, otpCode: String) {
        /*emailService.sendEmail(
            to = email,
            subject = "Your OTP Code",
            content = "Your OTP code is: $otpCode"
        )*/
    }

    override fun verifyOtp(email: String, otpCode: String): Boolean {

        return otpStorage[email] == otpCode
    }
}