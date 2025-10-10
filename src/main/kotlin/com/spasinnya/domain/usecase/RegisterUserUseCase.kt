package com.spasinnya.domain.usecase

import com.spasinnya.domain.port.PasswordHasher
import com.spasinnya.domain.port.TransactionRunner
import com.spasinnya.domain.repository.UserRepository
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class RegisterUserUseCase(
    private val users: UserRepository,
    private val hasher: PasswordHasher,
    private val tx: TransactionRunner,
) {

    suspend operator fun invoke(email: String, rawPassword: String): Result<Unit> = runCatching {
        val normalized = email.trim().lowercase()

        tx.inTransaction {
            val existing = users.findByEmail(normalized).getOrThrow()

            val pwdHash = hasher.hash(rawPassword)

            if (existing == null) {
                // 1) нет пользователя — создаём
                val userId = users.createUser(
                    email = normalized,
                    passwordHash = pwdHash,
                    status = "pending"
                ).getOrThrow()

                users.createEmptyProfile(userId).getOrThrow()

            } else {
                when (existing.status) {
                    "pending" -> {
                        // 2) уже есть pending — перезапускаем регистрацию
                        users.resetPendingUser(
                            userId = existing.id,
                            newPasswordHash = pwdHash
                        ).getOrThrow()

                        // профиль уже есть/не нужен — оставляем как есть
                    }
                    "active", "blocked" -> {
                        // 3) активный или заблокированный — запрещаем
                        throw IllegalArgumentException("User already exists")
                    }
                    else -> {
                        // на всякий случай, если добавятся новые статусы
                        throw IllegalStateException("Unsupported user status: ${existing.status}")
                    }
                }
            }

            // мок-OTP (при реальном OTP ещё и очистить/создать запись токена в otp_tokens)
            val otpCode = "1111"
            println("OTP for $normalized = $otpCode")
        }
    }
}