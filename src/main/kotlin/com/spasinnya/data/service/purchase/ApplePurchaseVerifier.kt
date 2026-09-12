@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.spasinnya.data.service.purchase

import com.spasinnya.domain.exception.PurchaseError
import com.spasinnya.domain.exception.PurchaseException
import com.spasinnya.domain.model.purchase.*
import com.spasinnya.domain.port.StorePurchaseVerifier
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** A production implementation must verify the JWS signature before returning any fields. */
fun interface AppleTransactionDecoder {
    suspend fun verifyAndDecode(signedTransaction: String): AppleTransaction
}

@Serializable
data class AppleTransaction(
    val transactionId: String,
    val productId: String,
    val bundleId: String,
    val purchaseDate: Long,
    val environment: String,
    val type: String = "Non-Consumable",
    val revocationDate: Long? = null,
    val ownershipType: String = "PURCHASED"
)

class ApplePurchaseVerifier(
    private val decoder: AppleTransactionDecoder,
    private val bundleId: String,
    private val allowedEnvironments: Set<StoreEnvironment>
) : StorePurchaseVerifier {
    init {
        require(bundleId.isNotBlank())
        require(allowedEnvironments.isNotEmpty())
    }

    override suspend fun verify(proof: PurchaseProof): VerifiedPurchase {
        if (proof.platform != StorePlatform.IOS) throw PurchaseException(PurchaseError.UNSUPPORTED_STORE)
        if (proof.value.isBlank() || proof.value.length > 65536) {
            throw PurchaseException(PurchaseError.INVALID_PROOF)
        }
        val transaction = decoder.verifyAndDecode(proof.value)
        if (transaction.bundleId != bundleId) throw PurchaseException(PurchaseError.WRONG_APPLICATION)
        val environment = when (transaction.environment) {
            "Sandbox" -> StoreEnvironment.SANDBOX
            "Production" -> StoreEnvironment.PRODUCTION
            else -> throw PurchaseException(PurchaseError.UNSUPPORTED_ENVIRONMENT)
        }
        if (environment !in allowedEnvironments) throw PurchaseException(PurchaseError.UNSUPPORTED_ENVIRONMENT)
        if (transaction.type != "Non-Consumable" || transaction.revocationDate != null ||
            transaction.ownershipType != "PURCHASED" ||
            transaction.transactionId.isBlank() || transaction.transactionId.length > 255 ||
            transaction.productId.isBlank() || transaction.productId.length > 255 ||
            transaction.purchaseDate <= 0) {
            throw PurchaseException(PurchaseError.INVALID_PROOF)
        }
        return VerifiedPurchase(
            platform = StorePlatform.IOS,
            storeProductId = transaction.productId,
            storeTransactionId = transaction.transactionId,
            purchasedAt = Instant.fromEpochMilliseconds(transaction.purchaseDate),
            environment = environment
        )
    }
}
