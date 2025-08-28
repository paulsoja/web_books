package com.spasinnya.domain.usecase

import com.spasinnya.domain.exception.BadRequestException
import com.spasinnya.domain.exception.InvalidOtpException
import com.spasinnya.domain.exception.UnauthorizedException
import com.spasinnya.domain.exception.UserNotFoundException
import com.spasinnya.domain.model.auth.AuthResponse
import com.spasinnya.domain.repository.JwtService
import com.spasinnya.domain.repository.OtpService
import com.spasinnya.domain.repository.UserRepository
import de.mkammerer.argon2.Argon2Factory

class AuthUseCase(
    private val userRepository: UserRepository,
    private val otpService: OtpService,
    private val jwtService: JwtService,
) {

    suspend fun register(email: String, password: String) {
        val user = userRepository.findByEmail(email).getOrNull()

        if (user != null) {
            throw BadRequestException("User already exists")
        }

        val otpCode = "1111"

        userRepository.createUser(email, hashPassword(password), otpCode)
        otpService.sendOtp(email, otpCode)
    }

    suspend fun verifyOtp(email: String, otpCode: String): AuthResponse {
        val user = userRepository.findByEmail(email).getOrNull()

        if (user?.isConfirmed == true) {
            throw BadRequestException()
        }

        if (user?.otpCode != otpCode) {
            throw InvalidOtpException("Invalid or expired OTP code for user $email")
        }

        userRepository.confirmUser(user.id)

        val accessToken = jwtService.generateAccessToken(user)
        val refreshToken = jwtService.generateRefreshToken(user)

        return AuthResponse(accessToken, refreshToken)
    }

    suspend fun login(email: String, password: String): AuthResponse {
        val user = userRepository.findByEmail(email).getOrNull()

        if (user == null) {
            throw UserNotFoundException()
        }

        if (!verifyPassword(password, user.password)) {
            throw UnauthorizedException("Invalid email or password")
        }
        if (!user.isConfirmed) {
            throw UnauthorizedException()
        }
        val accessToken = jwtService.generateAccessToken(user)
        val refreshToken = jwtService.generateRefreshToken(user)
        return AuthResponse(accessToken, refreshToken)
    }

    suspend fun sendResetPasswordOtp(email: String) {
        userRepository.findByEmail(email)
            .fold(
                onSuccess = {
                    otpService.sendOtp(it.email, "") // TODO add otp
                },
                onFailure = { throw UserNotFoundException("User not found") }
            )
    }

    suspend fun resetPassword(email: String, otpCode: String, newPassword: String) {
        if (!otpService.verifyOtp(email, otpCode)) {
            throw IllegalArgumentException("Invalid OTP")
        }
        userRepository.updatePassword(email, newPassword)
    }
}

fun hashPassword(password: String): String {
    val argon2 = Argon2Factory.create()
    return argon2.hash(2, 65536, 1, password.toCharArray()) // cost, memory, parallelism
}

fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
    val argon2 = Argon2Factory.create()
    return argon2.verify(hashedPassword, plainPassword.toCharArray())
}