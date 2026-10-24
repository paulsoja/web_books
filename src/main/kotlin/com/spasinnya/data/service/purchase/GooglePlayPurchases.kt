package com.spasinnya.data.service.purchase

import com.spasinnya.data.repository.AndroidAcknowledgementRepository
import com.spasinnya.domain.exception.PurchaseError
import com.spasinnya.domain.exception.PurchaseException
import com.spasinnya.domain.model.purchase.AndroidPurchaseIdentity
import com.spasinnya.domain.model.purchase.StorePlatform
import com.spasinnya.domain.model.purchase.VerifiedPurchase
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.server.application.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.v1.jdbc.Database
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

class GooglePlayPurchases(
    private val api: GooglePlayApi,
    private val acknowledgements: AndroidAcknowledgementRepository,
    allowTestPurchases: Boolean
) {
    val verifier = GooglePlayPurchaseVerifier(api, allowTestPurchases)

    suspend fun finish(purchase: VerifiedPurchase) {
        if (purchase.platform != StorePlatform.ANDROID) return
        acknowledge(purchase.storeProductId, requireNotNull(purchase.androidPurchaseToken))
    }

    private suspend fun acknowledge(productId: String, token: String) {
        val current = api.getPurchase(token)
        if (permanentProduct(current).productId != productId) throw PurchaseException(PurchaseError.INVALID_PROOF)
        if (current.acknowledgementState != "ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED") {
            api.acknowledge(productId, token)
        }
        acknowledgements.complete(AndroidPurchaseIdentity.tokenId(token))
    }

    suspend fun retryPending(onFailure: (Long) -> Unit) {
        for (pending in acknowledgements.pending()) {
            try {
                acknowledge(pending.productId, pending.token)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                acknowledgements.retryLater(pending.purchaseId)
                onFailure(pending.purchaseId)
            }
        }
    }

    companion object {
        fun configure(application: Application, database: Database, env: (String) -> String? = System::getenv): GooglePlayPurchases? {
            val enabled = env("GOOGLE_PLAY_ENABLED") ?: "false"
            require(enabled in setOf("true", "false")) { "GOOGLE_PLAY_ENABLED must be true or false" }
            if (enabled == "false") return null
            val packageName = requireNotNull(env("GOOGLE_PLAY_PACKAGE_NAME")) { "GOOGLE_PLAY_PACKAGE_NAME is required" }
            require(packageName.matches(Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+"))) { "Invalid Google Play package name" }
            val testSetting = env("GOOGLE_PLAY_ALLOW_TEST_PURCHASES") ?: "false"
            require(testSetting in setOf("true", "false")) { "GOOGLE_PLAY_ALLOW_TEST_PURCHASES must be true or false" }
            val allowTest = testSetting == "true"
            require(!allowTest || env("APP_ENV") in setOf("development", "test")) { "Test purchases require a separate development/test environment" }
            val path = requireNotNull(env("GOOGLE_PLAY_SERVICE_ACCOUNT_FILE")) { "GOOGLE_PLAY_SERVICE_ACCOUNT_FILE is required" }
            val client = HttpClient(CIO) {
                expectSuccess = false
                followRedirects = false
                install(HttpTimeout) {
                    requestTimeoutMillis = 20_000
                    connectTimeoutMillis = 10_000
                    socketTimeoutMillis = 20_000
                }
            }
            val runtime = try {
                val credentials = GoogleServiceAccountToken(client, File(path))
                GooglePlayPurchases(GooglePlayHttpApi(client, packageName, credentials::get),
                    AndroidAcknowledgementRepository(database), allowTest)
            } catch (failure: Exception) {
                client.close()
                throw failure
            }
            val worker = application.launch {
                while (isActive) {
                    try {
                        runtime.retryPending { id -> application.log.warn("Google purchase acknowledgement will retry; purchaseId={}", id) }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        application.log.error("Google purchase acknowledgement queue could not be processed")
                    }
                    delay(60_000.milliseconds)
                }
            }
            application.monitor.subscribe(ApplicationStopping) { worker.cancel(); client.close() }
            return runtime
        }
    }
}
