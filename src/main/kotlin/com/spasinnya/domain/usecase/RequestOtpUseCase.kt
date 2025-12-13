package com.spasinnya.domain.usecase

import com.spasinnya.data.repository.database.table.OtpPurpose
import com.spasinnya.data.service.security.OtpGenerator
import com.spasinnya.domain.model.auth.EmailMessage
import com.spasinnya.domain.port.EmailSender
import com.spasinnya.domain.port.OtpHasher
import com.spasinnya.domain.repository.OtpRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class RequestOtpUseCase(
    private val otps: OtpRepository,
    private val hasher: OtpHasher,
    private val generator: OtpGenerator,
    private val emailSender: EmailSender,
    private val clock: () -> Instant,
    private val ttl: Duration = 10.minutes,
    private val cooldown: Duration = 30.seconds
) {

    suspend operator fun invoke(
        email: String,
        purpose: OtpPurpose = OtpPurpose.LOGIN,
        userAgent: String? = null,
        ip: String? = null
    ): Result<Unit> = runCatching {
        val normalized = email.trim().lowercase()
        require(normalized.isNotBlank()) { "Email is blank" }
        require('@' in normalized) { "Invalid email" } // можно потом заменить на норм. валидатор

        val now = clock()

        val allowed = otps
            .canRequest(normalized, purpose, now, cooldownSeconds = cooldown.inWholeSeconds)
            .getOrThrow()

        if (!allowed) error("Too many requests")

        val code = generator.generate6Digits()
        val hash = hasher.hash(normalized, purpose, code)
        val expiresAt = now + ttl

        otps.create(
            email = normalized,
            purpose = purpose,
            codeHash = hash,
            createdAt = now,
            expiresAt = expiresAt,
            ip = ip,
            ua = userAgent
        ).getOrThrow()

        emailSender.send(
            EmailMessage(
                to = normalized,
                subject = "Your register code",
                text = buildString {
                    appendLine("Your code: $code")
                    appendLine("It expires in ${ttl.inWholeMinutes} minutes.")
                }
            )
        ).getOrThrow()
    }
}