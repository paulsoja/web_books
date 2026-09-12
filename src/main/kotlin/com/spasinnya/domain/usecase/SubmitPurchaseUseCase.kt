package com.spasinnya.domain.usecase

import com.spasinnya.domain.model.purchase.PurchaseProof
import com.spasinnya.domain.port.StorePurchaseVerifier
import com.spasinnya.domain.repository.PurchaseRepository

class SubmitPurchaseUseCase(
    private val verifier: StorePurchaseVerifier,
    private val purchases: PurchaseRepository
) {
    suspend operator fun invoke(userId: Long, proof: PurchaseProof): Result<Long> = runCatching {
        val verified = verifier.verify(proof)
        purchases.recordAndGrant(userId, verified).getOrThrow()
    }
}
