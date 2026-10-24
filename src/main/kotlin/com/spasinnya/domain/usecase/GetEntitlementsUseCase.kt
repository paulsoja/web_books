package com.spasinnya.domain.usecase

import com.spasinnya.domain.repository.EntitlementRepository

class GetEntitlementsUseCase(private val entitlements: EntitlementRepository) {
    suspend operator fun invoke(userId: Long): Result<List<Long>> = entitlements.findProductIdsByUser(userId)
}
