package com.spasinnya.data.service.purchase

import com.spasinnya.domain.exception.PurchaseError
import com.spasinnya.domain.exception.PurchaseException
import com.spasinnya.domain.model.purchase.StoreEnvironment
import com.spasinnya.domain.port.StorePurchaseVerifier
import kotlinx.serialization.json.Json
import java.io.File

object PurchaseVerificationConfig {
    fun createVerifier(env: (String) -> String? = System::getenv): StorePurchaseVerifier {
        return when (env("PURCHASE_VERIFICATION_MODE") ?: "disabled") {
            "disabled" -> StorePurchaseVerifier {
                throw PurchaseException(PurchaseError.VERIFICATION_UNAVAILABLE)
            }
            "mock" -> {
                require(env("APP_ENV") in setOf("development", "test")) {
                    "Mock purchases require APP_ENV=development or test"
                }
                val bundleId = requireNotNull(env("APPLE_BUNDLE_ID")) { "APPLE_BUNDLE_ID is required" }
                val path = requireNotNull(env("APPLE_MOCK_FIXTURES_FILE")) { "APPLE_MOCK_FIXTURES_FILE is required" }
                val fixtures = Json.decodeFromString<Map<String, AppleTransaction>>(File(path).readText())
                require(fixtures.isNotEmpty()) { "Mock fixtures must not be empty" }
                ApplePurchaseVerifier(MockAppleTransactionDecoder(fixtures), bundleId, setOf(StoreEnvironment.SANDBOX))
            }
            else -> error("Unsupported PURCHASE_VERIFICATION_MODE; real store verification is not configured yet")
        }
    }
}
