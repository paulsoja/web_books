package com.spasinnya.domain.usecase

import com.spasinnya.domain.repository.UserRepository

class GetUserProfileUseCase(private val userRepository: UserRepository) {
    suspend fun execute(email: String) = userRepository.findByEmail(email)
}