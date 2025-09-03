package com.spasinnya.data.service.security

import at.favre.lib.crypto.bcrypt.BCrypt
import com.spasinnya.domain.port.PasswordHasher

class BcryptPasswordHasher(
    private val cost: Int = (System.getenv("BCRYPT_COST") ?: "12").toInt(),
    private val pepper: String? = System.getenv("BCRYPT_PEPPER")
) : PasswordHasher {

    override fun hash(raw: String): String {
        val material = withPepper(raw)
        return BCrypt.withDefaults().hashToString(cost, material.toCharArray())
    }

    override fun verify(raw: String, hash: String): Boolean {
        val material = withPepper(raw)
        val res = BCrypt.verifyer().verify(material.toCharArray(), hash)
        return res.verified
    }

    private fun withPepper(raw: String): String =
        if (pepper.isNullOrEmpty()) raw else raw + pepper
}