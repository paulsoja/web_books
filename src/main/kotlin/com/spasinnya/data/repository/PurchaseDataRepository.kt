@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.spasinnya.data.repository

import com.spasinnya.data.extension.runDb
import com.spasinnya.data.repository.database.table.*
import com.spasinnya.domain.exception.PurchaseError
import com.spasinnya.domain.exception.PurchaseException
import com.spasinnya.domain.model.purchase.VerifiedPurchase
import com.spasinnya.domain.repository.PurchaseRepository
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.selectAll

class PurchaseDataRepository(private val database: Database) : PurchaseRepository {
    override suspend fun recordAndGrant(userId: Long, purchase: VerifiedPurchase): Result<Long> = database.runDb {
        // The unique constraint arbitrates concurrent submissions. Exposed retries SQL
        // serialization failures as a whole transaction at REPEATABLE READ isolation.
        val existing = Purchases.selectAll().where {
            (Purchases.platform eq purchase.platform) and
                (Purchases.storeTransactionId eq purchase.storeTransactionId)
        }.singleOrNull()
        val productId = if (existing != null) {
            existing[Purchases.productId]
        } else {
            StoreProductMappings.selectAll().where {
                (StoreProductMappings.platform eq purchase.platform) and
                    (StoreProductMappings.storeProductId eq purchase.storeProductId)
            }.singleOrNull()?.get(StoreProductMappings.productId)
                ?: throw PurchaseException(PurchaseError.UNKNOWN_PRODUCT)
        }
        Purchases.insertIgnore {
            it[Purchases.userId] = userId
            it[Purchases.platform] = purchase.platform
            it[Purchases.productId] = productId
            it[storeProductId] = purchase.storeProductId
            it[storeTransactionId] = purchase.storeTransactionId
            it[environment] = purchase.environment
            it[purchasedAt] = purchase.purchasedAt
        }
        val persisted = Purchases.selectAll().where {
            (Purchases.platform eq purchase.platform) and
                (Purchases.storeTransactionId eq purchase.storeTransactionId)
        }.single()
        if (persisted[Purchases.userId] != userId) {
            throw PurchaseException(PurchaseError.TRANSACTION_ALREADY_CLAIMED)
        }
        if (persisted[Purchases.storeProductId] != purchase.storeProductId ||
            persisted[Purchases.environment] != purchase.environment) {
            throw PurchaseException(PurchaseError.INVALID_PROOF)
        }
        val grantedProductId = persisted[Purchases.productId]
        Entitlements.insertIgnore {
            it[Entitlements.userId] = userId
            it[Entitlements.productId] = grantedProductId
        }
        grantedProductId
    }
}
