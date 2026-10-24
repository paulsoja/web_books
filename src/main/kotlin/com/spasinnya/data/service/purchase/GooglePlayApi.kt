package com.spasinnya.data.service.purchase

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.spasinnya.domain.exception.PurchaseError
import com.spasinnya.domain.exception.PurchaseException
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.util.Base64
import java.util.Date

@Serializable
data class GoogleProductPurchase(
    val productLineItem: List<GoogleProductLineItem> = emptyList(),
    val purchaseStateContext: GooglePurchaseState = GooglePurchaseState(),
    val testPurchaseContext: kotlinx.serialization.json.JsonObject? = null,
    val orderId: String? = null,
    val obfuscatedExternalAccountId: String? = null,
    val purchaseCompletionTime: String? = null,
    val acknowledgementState: String = ""
)

@Serializable
data class GooglePurchaseState(val purchaseState: String = "")

@Serializable
data class GoogleProductLineItem(val productId: String = "", val productOfferDetails: GoogleOfferDetails? = null)

@Serializable
data class GoogleOfferDetails(
    val quantity: Int = 0,
    val refundableQuantity: Int = 0,
    val consumptionState: String = "",
    val rentOfferDetails: kotlinx.serialization.json.JsonObject? = null,
    val preorderOfferDetails: kotlinx.serialization.json.JsonObject? = null
)

interface GooglePlayApi {
    suspend fun getPurchase(token: String): GoogleProductPurchase
    suspend fun acknowledge(productId: String, token: String)
}

/** Only fixed Google hosts are used; neither the client nor credential token_uri controls the destination. */
class GooglePlayHttpApi(
    private val client: HttpClient,
    private val packageName: String,
    private val accessToken: suspend () -> String
) : GooglePlayApi {
    private val json = Json { ignoreUnknownKeys = true }
    private val base = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/${packageName.encodeURLPathPart()}"

    override suspend fun getPurchase(token: String): GoogleProductPurchase = googleRequest {
        val response = client.get("$base/purchases/productsv2/tokens/${token.encodeURLPathPart()}") {
            bearerAuth(accessToken())
        }
        checkStatus(response.status)
        json.decodeFromString<GoogleProductPurchase>(response.bodyAsText())
    }

    override suspend fun acknowledge(productId: String, token: String): Unit = googleRequest {
        val response = client.post("$base/purchases/products/${productId.encodeURLPathPart()}/tokens/${token.encodeURLPathPart()}:acknowledge") {
            bearerAuth(accessToken())
            contentType(ContentType.Application.Json)
            setBody("{}")
        }
        checkStatus(response.status)
    }

    private fun checkStatus(status: HttpStatusCode) {
        when (status.value) {
            in 200..299 -> Unit
            400, 404, 410 -> throw PurchaseException(PurchaseError.INVALID_PROOF)
            else -> throw PurchaseException(PurchaseError.VERIFICATION_UNAVAILABLE)
        }
    }
}

/** A service-account JWT is exchanged for a short-lived OAuth token, cached until one minute before expiry. */
class GoogleServiceAccountToken(private val client: HttpClient, credentialsFile: File) {
    @Serializable
    private data class Credentials(val type: String, val client_email: String, val private_key: String)
    @Serializable
    private data class TokenResponse(val access_token: String, val expires_in: Long)

    private val json = Json { ignoreUnknownKeys = true }
    private val credentials = json.decodeFromString<Credentials>(credentialsFile.readText())
    private val algorithm: Algorithm
    private val mutex = Mutex()
    private var cachedToken: String? = null
    private var expiresAt = Instant.EPOCH

    init {
        require(credentials.type == "service_account") { "Google credentials must be a service account JSON key" }
        require(credentials.client_email.isNotBlank()) { "Google service account email is missing" }
        val encoded = credentials.private_key.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "").replace(Regex("\\s"), "")
        val key = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(Base64.getDecoder().decode(encoded))) as RSAPrivateKey
        algorithm = Algorithm.RSA256(null, key)
    }

    suspend fun get(): String = mutex.withLock {
        val now = Instant.now()
        cachedToken?.takeIf { now.isBefore(expiresAt) } ?: googleRequest {
            val assertion = withContext(Dispatchers.IO) {
                JWT.create().withIssuer(credentials.client_email)
                    .withClaim("scope", "https://www.googleapis.com/auth/androidpublisher")
                    .withAudience("https://oauth2.googleapis.com/token")
                    .withIssuedAt(Date.from(now)).withExpiresAt(Date.from(now.plusSeconds(3600)))
                    .sign(algorithm)
            }
            val response = client.submitForm("https://oauth2.googleapis.com/token", parameters {
                append("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                append("assertion", assertion)
            })
            if (response.status != HttpStatusCode.OK) throw PurchaseException(PurchaseError.VERIFICATION_UNAVAILABLE)
            val token = json.decodeFromString<TokenResponse>(response.bodyAsText())
            if (token.access_token.isBlank() || token.expires_in <= 60) throw PurchaseException(PurchaseError.VERIFICATION_UNAVAILABLE)
            cachedToken = token.access_token
            expiresAt = now.plusSeconds(token.expires_in - 60)
            token.access_token
        }
    }
}

// Do not propagate HTTP exceptions: their messages can contain the purchase token in the URL.
internal suspend fun <T> googleRequest(block: suspend () -> T): T = try {
    block()
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (expected: PurchaseException) {
    throw expected
} catch (_: Exception) {
    throw PurchaseException(PurchaseError.VERIFICATION_UNAVAILABLE)
}
