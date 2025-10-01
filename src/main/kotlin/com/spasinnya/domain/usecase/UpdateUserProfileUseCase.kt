package com.spasinnya.domain.usecase

import com.spasinnya.domain.exception.NotFoundException
import com.spasinnya.domain.model.UserProfile
import com.spasinnya.domain.port.TransactionRunner
import com.spasinnya.domain.repository.UserRepository

class UpdateUserProfileUseCase(
    private val profiles: UserRepository,
    private val tx: TransactionRunner
) {

    suspend operator fun invoke(
        userId: Long,
        firstName: String?,
        lastName: String?
    ): Result<UserProfile> = runCatching {
        // простая валидация
        firstName?.let { require(it.length <= 100) { "firstName too long" } }
        lastName ?.let { require(it.length <= 100) { "lastName too long" } }

        tx.inTransaction {
            profiles.updateProfile(userId, firstName, lastName).getOrThrow()
            profiles.findUserProfile(userId).map { it ?: throw NotFoundException("User not found") }.getOrThrow()
        }
    }
}