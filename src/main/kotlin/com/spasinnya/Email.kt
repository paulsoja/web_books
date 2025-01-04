package com.spasinnya

import com.spasinnya.data.service.EmailService

object EmailServiceSingleton {
    val instance = EmailService(
        host = "smtp-relay.brevo.com",
        port = 587,
        username = "82ca8f002@smtp-brevo.com",
        password = "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"
    )
}