@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.spasinnya

import com.spasinnya.data.repository.EntitlementDataRepository
import com.spasinnya.data.repository.PurchaseDataRepository
import com.spasinnya.data.repository.database.table.*
import com.spasinnya.data.service.purchase.*
import com.spasinnya.domain.model.purchase.*
import com.spasinnya.domain.usecase.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import org.h2.jdbcx.JdbcConnectionPool

class PurchaseTestDatabase {
    private val pool = JdbcConnectionPool.create(
        "jdbc:h2:mem:purchases_${UUID.randomUUID()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "sa", ""
    )
    val database = Database.connect(pool)
    fun close() = pool.dispose()
    val purchases = PurchaseDataRepository(database)
    val entitlements = EntitlementDataRepository(database)
    val valid = AppleTransaction("tx-1", "ios.book.1", "test.app", 1780000000000, "Sandbox")
    val fixtures = mapOf(
        "valid" to valid,
        "second" to valid.copy(transactionId = "tx-2"),
        "wrong-app" to valid.copy(bundleId = "other.app"),
        "unknown" to valid.copy(productId = "unknown"),
        "revoked" to valid.copy(revocationDate = 1780000001000),
        "subscription" to valid.copy(type = "Auto-Renewable Subscription"),
        "consumable" to valid.copy(type = "Consumable"),
        "production" to valid.copy(environment = "Production"),
        "xcode" to valid.copy(environment = "Xcode"),
        "shared" to valid.copy(ownershipType = "FAMILY_SHARED")
    )
    val verifier = ApplePurchaseVerifier(MockAppleTransactionDecoder(fixtures), "test.app", setOf(StoreEnvironment.SANDBOX))
    val submit = SubmitPurchaseUseCase(verifier, purchases)

    init {
        transaction(database) {
            maxAttempts = 1
            SchemaUtils.create(Users, Books)
            // Exercise the actual Flyway SQL, including constraints, rather than duplicate its schema.
            val migration = checkNotNull(javaClass.getResource("/db/migration/V8__store_purchases_and_entitlements.sql")).readText()
            migration.split(";").filter { it.isNotBlank() }.forEach { exec(it) }
            Users.insert { it[id] = 1; it[email] = "one@test.example" }
            Users.insert { it[id] = 2; it[email] = "two@test.example" }
            Books.insert { it[id] = 1; it[number] = "1"; it[title] = "Test book" }
            StoreProductMappings.insert {
                it[platform] = StorePlatform.IOS
                it[storeProductId] = "ios.book.1"
                it[productId] = 1
            }
            StoreProductMappings.insert {
                it[platform] = StorePlatform.ANDROID
                it[storeProductId] = "android.book.1"
                it[productId] = 1
            }
        }
    }

    fun countPurchases(): Long = transaction(database) { Purchases.selectAll().count() }
    fun countEntitlements(): Long = transaction(database) { Entitlements.selectAll().count() }
}
