@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.spasinnya.data.service.purchase

import com.spasinnya.domain.exception.PurchaseError
import com.spasinnya.domain.exception.PurchaseException
import com.spasinnya.domain.model.purchase.*
import com.spasinnya.domain.port.StorePurchaseVerifier
import kotlin.time.Instant

class GooglePlayPurchaseVerifier(
    private val api: GooglePlayApi,
    private val allowTestPurchases: Boolean = false
) : StorePurchaseVerifier {
    override suspend fun verify(proof: PurchaseProof): VerifiedPurchase {
        if (proof.platform != StorePlatform.ANDROID) throw PurchaseException(PurchaseError.UNSUPPORTED_STORE)
        if (proof.value.isBlank() || proof.value.length > 8192) invalid()
        val purchase = api.getPurchase(proof.value)
        val item = permanentProduct(purchase)
        if (proof.accountId.isNullOrBlank() || purchase.obfuscatedExternalAccountId != proof.accountId) {
            throw PurchaseException(PurchaseError.WRONG_ACCOUNT)
        }
        val environment = if (purchase.testPurchaseContext != null) StoreEnvironment.SANDBOX else StoreEnvironment.PRODUCTION
        if (environment == StoreEnvironment.SANDBOX && !allowTestPurchases) {
            throw PurchaseException(PurchaseError.UNSUPPORTED_ENVIRONMENT)
        }
        val purchasedAt = try { Instant.parse(purchase.purchaseCompletionTime ?: invalid()) }
        catch (_: IllegalArgumentException) { invalid() }
        if (purchasedAt <= Instant.fromEpochMilliseconds(0)) invalid()
        if ((purchase.orderId?.length ?: 0) > 255) invalid()
        return VerifiedPurchase(StorePlatform.ANDROID, item.productId,
            AndroidPurchaseIdentity.tokenId(proof.value), purchasedAt, environment, proof.value, purchase.orderId)
    }
}

/** Fail closed for pending/cancelled, rentals, preorders, consumed and refunded products. */
internal fun permanentProduct(purchase: GoogleProductPurchase): GoogleProductLineItem {
    if (purchase.purchaseStateContext.purchaseState != "PURCHASED") invalid()
    if (purchase.acknowledgementState !in setOf("ACKNOWLEDGEMENT_STATE_PENDING", "ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED")) invalid()
    val item = purchase.productLineItem.singleOrNull() ?: invalid()
    val offer = item.productOfferDetails ?: invalid()
    if (item.productId.isBlank() || item.productId.length > 255 || offer.quantity != 1 || offer.refundableQuantity != 1 ||
        offer.consumptionState != "CONSUMPTION_STATE_YET_TO_BE_CONSUMED" ||
        offer.rentOfferDetails != null || offer.preorderOfferDetails != null) invalid()
    return item
}

private fun invalid(): Nothing = throw PurchaseException(PurchaseError.INVALID_PROOF)
