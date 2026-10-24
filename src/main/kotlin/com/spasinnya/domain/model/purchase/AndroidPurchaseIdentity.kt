package com.spasinnya.domain.model.purchase

import java.security.MessageDigest

object AndroidPurchaseIdentity {
    fun tokenId(token: String): String = sha256(token)
    fun accountId(userId: Long): String = sha256("books-user:$userId")
    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
