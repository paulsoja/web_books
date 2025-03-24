package com.spasinnya

import com.spasinnya.data.service.EmailService

object EmailServiceSingleton {
    val instance = EmailService(
        host = "smtp-relay.brevo.com",
        port = 587,
        username = "82ca8f002@smtp-brevo.com",
        password = System.getenv("BREVO_PASS") ?: error("BREVO_PASS is missing!")
    )
}