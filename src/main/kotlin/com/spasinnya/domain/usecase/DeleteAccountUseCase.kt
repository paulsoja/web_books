package com.spasinnya.domain.usecase

import com.spasinnya.domain.repository.UserRepository

class DeleteAccountUseCase(private val users: UserRepository) {
    suspend operator fun invoke(userId: Long): Result<Unit> = users.deleteAccount(userId)
}
