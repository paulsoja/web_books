package com.spasinnya.data.service

import java.util.*
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage

class EmailService(
    private val host: String = "smtp-relay.brevo.com",
    private val port: Int = 587,
    private val username: String = "82ca8f002@smtp-brevo.com",
    private val password: String = "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"
) {

    private val session: Session = Session.getInstance(Properties().apply {
        put("mail.smtp.host", host)
        put("mail.smtp.port", port.toString())
        put("mail.smtp.auth", "true")
        //put("mail.smtp.port", "465")
        //put("mail.smtp.ssl.enable", "true")
        put("mail.smtp.starttls.enable", "true") // Используем TLS
        put("mail.debug", "true")
    }, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            println("getPasswordAuthentication: username=$username; password=$password")
            return PasswordAuthentication(password, "")
        }
    })

    fun sendEmail(to: String, subject: String, content: String) {
        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                setRecipient(Message.RecipientType.TO, InternetAddress(to))
                this.subject = subject
                setText(content)
            }
            Transport.send(message)
            println("Email sent to $to")
        } catch (e: MessagingException) {
            e.printStackTrace()
            throw RuntimeException("Failed to send email: ${e.message}")
        }
    }
}