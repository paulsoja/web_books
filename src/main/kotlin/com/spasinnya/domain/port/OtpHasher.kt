package com.spasinnya.domain.port

import com.spasinnya.data.repository.database.table.OtpPurpose
import java.security.MessageDigest

class OtpHasher(private val pepper: String) {

    fun hash(email: String, purpose: OtpPurpose, code: String): String {
        val input = "$pepper|${email.lowercase()}|${purpose.name}|$code"
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun equalsSafe(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))
}