package com.spasinnya.data.service.security

import com.spasinnya.domain.model.auth.EmailMessage
import com.spasinnya.domain.port.EmailSender
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class BrevoEmailSender(
    private val client: HttpClient,
    private val apiKey: String,
    private val fromEmail: String
) : EmailSender {

    @Serializable
    private data class Sender(val email: String)

    @Serializable
    private data class To(val email: String)

    @Serializable
    private data class Req(
        val sender: Sender,
        val to: List<To>,
        val subject: String,
        @SerialName("textContent") val textContent: String
    )

    override suspend fun send(message: EmailMessage): Result<Unit> = runCatching {
        val resp = client.post("https://api.brevo.com/v3/smtp/email") {
            header("api-key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(
                Req(
                    sender = Sender(fromEmail),
                    to = listOf(To(message.to)),
                    subject = message.subject,
                    textContent = message.text
                )
            )
        }

        if (resp.status.value !in 200..299) {
            // чтобы сразу видеть причину от Brevo:
            val bodyText = resp.bodyAsText()
            error("Brevo error: ${resp.status}. Body: $bodyText")
        }
    }
}