package com.spasinnya.data.service.security

import java.security.SecureRandom

class OtpGenerator(
    private val random: SecureRandom = SecureRandom()
) {
    fun generate6Digits(): String =
        random.nextInt(1_000_000).toString().padStart(6, '0')
}
