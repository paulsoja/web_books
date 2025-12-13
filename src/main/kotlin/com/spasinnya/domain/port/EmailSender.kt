package com.spasinnya.domain.port

import com.spasinnya.domain.model.auth.EmailMessage

interface EmailSender {
    suspend fun send(message: EmailMessage): Result<Unit>
}