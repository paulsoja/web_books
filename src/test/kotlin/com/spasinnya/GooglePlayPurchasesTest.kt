@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.spasinnya

import com.spasinnya.data.repository.AndroidAcknowledgementRepository
import com.spasinnya.data.repository.database.table.AndroidPurchaseAcknowledgements as Ack
import com.spasinnya.data.service.purchase.*
import com.spasinnya.domain.exception.*
import com.spasinnya.domain.model.purchase.*
import com.spasinnya.domain.usecase.SubmitPurchaseUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.*

class GooglePlayPurchasesTest {
    private val db = PurchaseTestDatabase()
    @AfterTest fun close() = db.close()
    private val account = AndroidPurchaseIdentity.accountId(1)
    private val proof = PurchaseProof(StorePlatform.ANDROID, "google-token", account)
    private val offer = GoogleOfferDetails(1, 1, "CONSUMPTION_STATE_YET_TO_BE_CONSUMED")
    private val valid = GoogleProductPurchase(
        listOf(GoogleProductLineItem("android.book.1", offer)), GooglePurchaseState("PURCHASED"),
        orderId = "GPA.test", obfuscatedExternalAccountId = account,
        purchaseCompletionTime = "2026-10-17T10:00:00Z", acknowledgementState = "ACKNOWLEDGEMENT_STATE_PENDING"
    )
    private class Api(var purchase: GoogleProductPurchase) : GooglePlayApi {
        var acknowledgements = 0
        var failAcknowledgement = false
        override suspend fun getPurchase(token: String) = purchase
        override suspend fun acknowledge(productId: String, token: String) {
            if (failAcknowledgement) throw PurchaseException(PurchaseError.VERIFICATION_UNAVAILABLE)
            acknowledgements++
            purchase = purchase.copy(acknowledgementState = "ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED")
        }
    }

    @Test fun `Google purchase persists access and acknowledgement and retries idempotently`() = runBlocking {
        val api = Api(valid)
        val runtime = GooglePlayPurchases(api, AndroidAcknowledgementRepository(db.database), false)
        val submit = SubmitPurchaseUseCase(runtime.verifier, db.purchases, runtime::finish)
        assertEquals(1L, submit(1, proof).getOrThrow())
        assertEquals(1L, submit(1, proof).getOrThrow())
        assertEquals(1L, db.countPurchases())
        assertEquals(1L, db.countEntitlements())
        assertEquals(1, api.acknowledgements)
        transaction(db.database) {
            val row = Ack.selectAll().single()
            assertEquals("google-token", row[Ack.purchaseToken])
            assertEquals("GPA.test", row[Ack.googleOrderId])
            assertTrue(row[Ack.acknowledged])
        }
        val stolen = submit(2, proof.copy(accountId = AndroidPurchaseIdentity.accountId(2)))
        assertEquals(PurchaseError.WRONG_ACCOUNT, (stolen.exceptionOrNull() as PurchaseException).error)
        val replay = submit(2, proof)
        assertEquals(PurchaseError.TRANSACTION_ALREADY_CLAIMED, (replay.exceptionOrNull() as PurchaseException).error)
    }

    @Test fun `acknowledgement failure leaves durable work which survives runtime recreation`() = runBlocking {
        val api = Api(valid).apply { failAcknowledgement = true }
        val runtime = GooglePlayPurchases(api, AndroidAcknowledgementRepository(db.database), false)
        val result = SubmitPurchaseUseCase(runtime.verifier, db.purchases, runtime::finish)(1, proof)
        assertEquals(PurchaseError.VERIFICATION_UNAVAILABLE, (result.exceptionOrNull() as PurchaseException).error)
        assertEquals(1L, db.countPurchases())
        assertEquals(listOf(1L), db.entitlements.findProductIdsByUser(1).getOrThrow())
        api.failAcknowledgement = false
        GooglePlayPurchases(api, AndroidAcknowledgementRepository(db.database), false).retryPending { fail("Unexpected retry failure") }
        assertEquals(1, api.acknowledgements)
        assertTrue(AndroidAcknowledgementRepository(db.database).pending().isEmpty())
    }

    @Test fun `invalid Google states do not save or acknowledge purchases`() = runBlocking {
        val invalid = listOf(
            valid.copy(purchaseStateContext = GooglePurchaseState("PENDING")),
            valid.copy(purchaseStateContext = GooglePurchaseState("CANCELLED")),
            valid.copy(obfuscatedExternalAccountId = null),
            valid.copy(obfuscatedExternalAccountId = "another-user"),
            valid.copy(purchaseCompletionTime = null),
            valid.copy(purchaseCompletionTime = "invalid"),
            valid.copy(acknowledgementState = "UNKNOWN"),
            valid.copy(productLineItem = emptyList()),
            valid.copy(productLineItem = valid.productLineItem + valid.productLineItem),
            valid.copy(productLineItem = listOf(GoogleProductLineItem("android.book.1"))),
            valid.copy(productLineItem = listOf(GoogleProductLineItem("android.book.1", offer.copy(quantity = 2)))),
            valid.copy(productLineItem = listOf(GoogleProductLineItem("android.book.1", offer.copy(refundableQuantity = 0)))),
            valid.copy(productLineItem = listOf(GoogleProductLineItem("android.book.1", offer.copy(consumptionState = "CONSUMPTION_STATE_CONSUMED")))),
            valid.copy(productLineItem = listOf(GoogleProductLineItem("android.book.1", offer.copy(rentOfferDetails = JsonObject(emptyMap()))))),
            valid.copy(productLineItem = listOf(GoogleProductLineItem("android.book.1", offer.copy(preorderOfferDetails = JsonObject(emptyMap()))))),
            valid.copy(testPurchaseContext = JsonObject(emptyMap())),
            valid.copy(productLineItem = listOf(GoogleProductLineItem("unknown-book", offer)))
        )
        for (purchase in invalid) {
            val api = Api(purchase)
            val runtime = GooglePlayPurchases(api, AndroidAcknowledgementRepository(db.database), false)
            assertTrue(SubmitPurchaseUseCase(runtime.verifier, db.purchases, runtime::finish)(1, proof).isFailure)
            assertEquals(0, api.acknowledgements)
        }
        assertEquals(0L, db.countPurchases())
        assertEquals(0L, db.countEntitlements())
        assertTrue(AndroidAcknowledgementRepository(db.database).pending().isEmpty())
    }

    @Test fun `test purchases must be explicitly allowed and token identity does not depend on order id`() = runBlocking {
        val verifier = GooglePlayPurchaseVerifier(Api(valid.copy(orderId = null, testPurchaseContext = JsonObject(emptyMap()))), true)
        val result = verifier.verify(proof)
        assertEquals(StoreEnvironment.SANDBOX, result.environment)
        assertEquals(AndroidPurchaseIdentity.tokenId(proof.value), result.storeTransactionId)
        assertEquals(64, result.storeTransactionId.length)
        assertFailsWith<PurchaseException> { verifier.verify(proof.copy(value = "")) }
        assertFailsWith<PurchaseException> { verifier.verify(proof.copy(value = "x".repeat(8193))) }
        assertFailsWith<PurchaseException> { verifier.verify(proof.copy(platform = StorePlatform.IOS)) }
        Unit
    }

    @Test fun `outbox insertion failure rolls back purchase and entitlement`() = runBlocking {
        transaction(db.database) { exec("ALTER TABLE android_purchase_acknowledgements ADD CONSTRAINT fail_outbox CHECK (purchase_id < 0)") }
        val api = Api(valid)
        val runtime = GooglePlayPurchases(api, AndroidAcknowledgementRepository(db.database), false)
        assertTrue(SubmitPurchaseUseCase(runtime.verifier, db.purchases, runtime::finish)(1, proof).isFailure)
        assertEquals(0L, db.countPurchases())
        assertEquals(0L, db.countEntitlements())
        assertEquals(0, api.acknowledgements)
    }
}
