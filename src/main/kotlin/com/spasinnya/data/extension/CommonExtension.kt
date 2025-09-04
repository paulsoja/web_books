package com.spasinnya.data.extension

import java.security.MessageDigest

fun sha256(value: String): String {
    val d = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
    return d.joinToString("") { "%02x".format(it) }
}