package com.spasinnya.data.service.security

import com.spasinnya.domain.model.auth.EmailMessage
import com.spasinnya.domain.port.EmailSender
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

class HttpEmailSender(
    private val client: HttpClient,
    private val apiBaseUrl: String,
    private val apiKey: String,
    private val fromEmail: String
) : EmailSender {

    override suspend fun send(message: EmailMessage): Result<Unit> = runCatching {
        // Тут адаптируй payload под конкретного провайдера
        client.post("$apiBaseUrl/send") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "from" to fromEmail,
                    "to" to message.to,
                    "subject" to message.subject,
                    "text" to message.text
                )
            )
        }.let { resp ->
            if (resp.status.value !in 200..299) error("Email provider error: ${resp.status}")
        }
    }
}
