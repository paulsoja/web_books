package com.spasinnya.domain.usecase

import com.spasinnya.data.repository.database.table.OtpPurpose
import com.spasinnya.domain.model.auth.LoginResult
import com.spasinnya.domain.port.OtpHasher
import com.spasinnya.domain.port.TokenService
import com.spasinnya.domain.repository.OtpRepository
import com.spasinnya.domain.repository.RefreshTokenRepository
import com.spasinnya.domain.repository.UserRepository
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class VerifyEmailOtpUseCase @OptIn(ExperimentalTime::class) constructor(
    private val users: UserRepository,
    private val refresh: RefreshTokenRepository,
    private val tokens: TokenService,
    private val otps: OtpRepository,
    private val hasher: OtpHasher,
    private val clock: () -> Instant,
    private val maxAttempts: Int = 5
) {

    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(
        email: String,
        code: String,
        userAgent: String? = null,
        ip: String? = null,
        deviceId: String? = null
    ): Result<LoginResult> = runCatching {
        val normalized = email.trim().lowercase()
        val otpCode = code.trim()

        val now = clock()

        val otp = otps.findLatestActive(normalized, OtpPurpose.LOGIN, now).getOrThrow()
            ?: error("OTP not found or expired")

        if (otp.attempts >= maxAttempts) error("Too many attempts")

        val expectedHash = otp.codeHash
        val actualHash = hasher.hash(normalized, OtpPurpose.LOGIN, otpCode)

        if (!hasher.equalsSafe(expectedHash, actualHash)) {
            otps.incrementAttempts(otp.id).getOrThrow()
            error("Invalid code")
        }

        // помечаем использованным
        otps.consume(otp.id, now).getOrThrow()

        val user = users.findByEmail(normalized).getOrThrow() ?: error("User not found")

        users.setConfirmedAt(user.id).getOrThrow()
        users.updateLastLogin(user.id).getOrThrow()

        val issued = tokens.issueTokens(user.id, user.email)

        refresh.save(
            userId = user.id,
            tokenHash = issued.refreshTokenHash,
            expiresAt = issued.refreshExpiresAt,
            userAgent = userAgent,
            ip = ip,
            deviceId = deviceId
        ).getOrThrow()

        LoginResult(
            accessToken = issued.accessToken,
            accessExpiresAt = issued.accessExpiresAt,
            refreshToken = issued.refreshToken,
            refreshExpiresAt = issued.refreshExpiresAt
        )
    }
}