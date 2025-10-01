package com.spasinnya.domain.usecase

import com.spasinnya.domain.exception.NotFoundException
import com.spasinnya.domain.model.UserProfile
import com.spasinnya.domain.repository.UserRepository

class GetUserProfileUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(userId: Long): Result<UserProfile> {
        return userRepository.findUserProfile(userId).map { it ?: throw NotFoundException("User not found") }
    }
}