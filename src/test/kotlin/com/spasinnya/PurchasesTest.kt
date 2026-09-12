@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.spasinnya

import com.spasinnya.data.repository.database.table.*
import com.spasinnya.data.service.purchase.*
import com.spasinnya.domain.exception.*
import com.spasinnya.domain.model.purchase.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.*

class PurchasesTest {
    private val db = PurchaseTestDatabase()
    @AfterTest fun closeDatabase() = db.close()

    private fun proof(value: String = "valid") = PurchaseProof(StorePlatform.IOS, value)

    @Test
    fun `verified transaction grants once and binds to user`() = runBlocking {
        assertEquals(1L, db.submit(1, proof()).getOrThrow())
        assertEquals(1L, db.submit(1, proof()).getOrThrow())
        assertEquals(1L, db.submit(1, proof("second")).getOrThrow())
        assertEquals(2L, db.countPurchases())
        assertEquals(1L, db.countEntitlements())
        assertEquals(listOf(1L), db.entitlements.findProductIdsByUser(1).getOrThrow())
        assertEquals(emptyList(), db.entitlements.findProductIdsByUser(2).getOrThrow())
        val error = db.submit(2, proof()).exceptionOrNull()
        assertEquals(PurchaseError.TRANSACTION_ALREADY_CLAIMED, (error as PurchaseException).error)
        assertEquals(1L, db.countEntitlements())
    }

    @Test
    fun `invalid app product environment and purchase types are rejected`() = runBlocking {
        val cases = mapOf(
            "" to PurchaseError.INVALID_PROOF,
            "unverified.jws.payload" to PurchaseError.INVALID_PROOF,
            "wrong-app" to PurchaseError.WRONG_APPLICATION,
            "unknown" to PurchaseError.UNKNOWN_PRODUCT,
            "revoked" to PurchaseError.INVALID_PROOF,
            "subscription" to PurchaseError.INVALID_PROOF,
            "consumable" to PurchaseError.INVALID_PROOF,
            "production" to PurchaseError.UNSUPPORTED_ENVIRONMENT,
            "xcode" to PurchaseError.UNSUPPORTED_ENVIRONMENT,
            "shared" to PurchaseError.INVALID_PROOF
        )
        for ((token, expected) in cases) {
            val error = db.submit(1, proof(token)).exceptionOrNull()
            assertEquals(expected, (error as PurchaseException).error, token)
        }
        assertEquals(0L, db.countPurchases())
        assertEquals(0L, db.countEntitlements())
    }

    @Test
    fun `entitlement insert failure rolls back purchase`() = runBlocking {
        transaction(db.database) {
            exec("ALTER TABLE entitlements ADD CONSTRAINT fail_grant CHECK (product_id <> 1)")
        }
        assertTrue(db.submit(1, proof()).isFailure)
        assertEquals(0L, db.countPurchases())
        assertEquals(0L, db.countEntitlements())
        transaction(db.database) { exec("ALTER TABLE entitlements DROP CONSTRAINT fail_grant") }
        assertEquals(1L, db.submit(1, proof()).getOrThrow())
    }

    @Test
    fun `concurrent duplicate submissions succeed only once in storage`() = runBlocking {
        val results = (1..8).map {
            async(Dispatchers.IO) { db.submit(1, proof()).getOrThrow() }
        }.awaitAll()
        assertEquals(List(8) { 1L }, results)
        assertEquals(1L, db.countPurchases())
        assertEquals(1L, db.countEntitlements())
    }

    @Test
    fun `concurrent users cannot claim the same transaction`() = runBlocking {
        val results = listOf(1L, 2L).map { user ->
            async(Dispatchers.IO) { db.submit(user, proof()) }
        }.awaitAll()
        assertEquals(1, results.count { it.isSuccess })
        assertEquals(PurchaseError.TRANSACTION_ALREADY_CLAIMED,
            (results.single { it.isFailure }.exceptionOrNull() as PurchaseException).error)
        assertEquals(1L, db.countPurchases())
        assertEquals(1L, db.countEntitlements())
    }

    @Test
    fun `shared domain product works across stores`() = runBlocking {
        db.submit(1, proof()).getOrThrow()
        // A future Google adapter produces the same platform-neutral value.
        val android = db.verifier.verify(proof()).copy(
            platform = StorePlatform.ANDROID, storeProductId = "android.book.1"
        )
        assertEquals(1L, db.purchases.recordAndGrant(1, android).getOrThrow())
        assertEquals(2L, db.countPurchases())
        assertEquals(1L, db.countEntitlements())
    }

    @Test
    fun `production policy and unsupported store are explicit`() = runBlocking {
        val production = ApplePurchaseVerifier(
            MockAppleTransactionDecoder(db.fixtures), "test.app", setOf(StoreEnvironment.PRODUCTION)
        )
        assertEquals(StoreEnvironment.PRODUCTION, production.verify(proof("production")).environment)
        assertEquals(PurchaseError.UNSUPPORTED_STORE, assertFailsWith<PurchaseException> {
            production.verify(PurchaseProof(StorePlatform.ANDROID, "production"))
        }.error)
    }

    @Test
    fun `mock is opt in and cannot be enabled in production`() = runBlocking {
        val disabled = PurchaseVerificationConfig.createVerifier { null }
        assertEquals(PurchaseError.VERIFICATION_UNAVAILABLE, assertFailsWith<PurchaseException> {
            disabled.verify(proof())
        }.error)
        assertFailsWith<IllegalArgumentException> {
            PurchaseVerificationConfig.createVerifier {
                mapOf("PURCHASE_VERIFICATION_MODE" to "mock", "APP_ENV" to "production")[it]
            }
        }
        Unit
    }

    @Test
    fun `documented development configuration verifies the sample fixture`() = runBlocking {
        val config = mapOf(
            "PURCHASE_VERIFICATION_MODE" to "mock",
            "APP_ENV" to "development",
            "APPLE_BUNDLE_ID" to "dev.books.app",
            "APPLE_MOCK_FIXTURES_FILE" to "docs/examples/apple-purchase-fixtures.json"
        )
        val verifier = PurchaseVerificationConfig.createVerifier { config[it] }
        assertEquals("dev.book.1.ios", verifier.verify(proof("dev-book-1-transaction-1")).storeProductId)
    }

    @Test
    fun `legacy purchase does not grant access`() = runBlocking {
        transaction(db.database) {
            SchemaUtils.create(UserPurchases)
            UserPurchases.insert {
                it[userId] = 1
                it[bookId] = 1
                it[platform] = "manual"
                it[storeProductId] = "manual"
                it[purchaseToken] = "legacy"
            }
        }
        assertEquals(emptyList(), db.entitlements.findProductIdsByUser(1).getOrThrow())
    }
}
