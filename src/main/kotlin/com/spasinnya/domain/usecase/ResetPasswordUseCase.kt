package com.spasinnya.domain.usecase

import com.spasinnya.data.repository.database.table.OtpPurpose
import com.spasinnya.domain.port.OtpHasher
import com.spasinnya.domain.port.PasswordHasher
import com.spasinnya.domain.repository.OtpRepository
import com.spasinnya.domain.repository.UserRepository
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class ResetPasswordUseCase(
    private val users: UserRepository,
    private val otps: OtpRepository,
    private val otpHasher: OtpHasher,
    private val passwordHasher: PasswordHasher,
    private val clock: () -> Instant,
    private val maxAttempts: Int = 5
) {

    suspend operator fun invoke(
        email: String,
        code: String,
        newPassword: String
    ): Result<Unit> = runCatching {
        val normalized = email.trim().lowercase()
        val otpCode = code.trim()

        val now = clock()

        val otp = otps.findLatestActive(normalized, OtpPurpose.PASSWORD_RESET, now).getOrThrow()
            ?: error("OTP not found or expired")

        if (otp.attempts >= maxAttempts) error("Too many attempts")

        val expectedHash = otp.codeHash
        val actualHash = otpHasher.hash(normalized, OtpPurpose.PASSWORD_RESET, otpCode)

        if (!otpHasher.equalsSafe(expectedHash, actualHash)) {
            otps.incrementAttempts(otp.id).getOrThrow()
            error("Invalid code")
        }

        val user = users.findByEmail(normalized).getOrThrow() ?: error("User not found")

        val newHash = passwordHasher.hash(newPassword)
        users.updatePassword(user.id, newHash).getOrThrow()
        
        if (user.status == "pending") {
            users.setConfirmedAt(user.id).getOrThrow()
        }

        otps.consume(otp.id, now).getOrThrow()
    }
}
