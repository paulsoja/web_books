package com.spasinnya.data.service.security

import java.security.SecureRandom

class OtpGenerator(
    private val random: SecureRandom = SecureRandom()
) {
    fun generate4Digits(): String =
        random.nextInt(1_000_000).toString().padStart(4, '0')
}
