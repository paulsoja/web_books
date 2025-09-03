package com.spasinnya.domain.usecase

import com.spasinnya.domain.model.User
import com.spasinnya.domain.repository.UserRepository

class GetUserProfileUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(email: String): Result<User?> {
        return userRepository.findByEmail(email)
    }
}