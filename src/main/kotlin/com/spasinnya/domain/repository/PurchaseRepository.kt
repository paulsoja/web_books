package com.spasinnya.domain.repository

import com.spasinnya.domain.model.purchase.VerifiedPurchase

interface PurchaseRepository {
    /** Resolve the catalog and atomically persist the transaction and grant access. */
    suspend fun recordAndGrant(userId: Long, purchase: VerifiedPurchase): Result<Long>
}
