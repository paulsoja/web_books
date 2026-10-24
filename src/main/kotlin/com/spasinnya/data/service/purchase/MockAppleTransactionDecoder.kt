package com.spasinnya.data.service.purchase

import com.spasinnya.domain.exception.PurchaseError
import com.spasinnya.domain.exception.PurchaseException

/**
 * Development only: opaque proofs are looked up in SERVER-owned fixtures.
 * Never decode arbitrary client JSON/JWS as trusted purchase data.
 */
class MockAppleTransactionDecoder(fixtures: Map<String, AppleTransaction>) : AppleTransactionDecoder {
    private val fixtures = fixtures.toMap()

    override suspend fun verifyAndDecode(signedTransaction: String): AppleTransaction =
        fixtures[signedTransaction] ?: throw PurchaseException(PurchaseError.INVALID_PROOF)
}
