package com.spasinnya.domain.port

import com.spasinnya.domain.model.purchase.PurchaseProof
import com.spasinnya.domain.model.purchase.VerifiedPurchase

fun interface StorePurchaseVerifier {
    suspend fun verify(proof: PurchaseProof): VerifiedPurchase
}
