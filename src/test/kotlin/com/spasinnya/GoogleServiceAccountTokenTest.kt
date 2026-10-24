package com.spasinnya

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.spasinnya.data.service.purchase.GoogleServiceAccountToken
import io.ktor.http.*
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.*
import java.nio.file.Files
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.util.Base64
import kotlin.test.*

class GoogleServiceAccountTokenTest {
    @Test fun `service account signs OAuth JWT and caches token across concurrent requests`() = testApplication {
        val pair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val privatePem = "-----BEGIN PRIVATE KEY-----\n${Base64.getEncoder().encodeToString(pair.private.encoded)}\n-----END PRIVATE KEY-----\n"
        val credentials = Files.createTempFile("books-google-test-", ".json").toFile()
        credentials.writeText(buildJsonObject {
            put("type", "service_account")
            put("client_email", "books@test.iam.gserviceaccount.com")
            put("private_key", privatePem)
            // The adapter must ignore credential-controlled destinations.
            put("token_uri", "https://untrusted.invalid/token")
        }.toString())
        var exchanges = 0
        externalServices {
            hosts("https://oauth2.googleapis.com") {
                routing {
                    post("/token") {
                        val form = call.receiveParameters()
                        assertEquals("urn:ietf:params:oauth:grant-type:jwt-bearer", form["grant_type"])
                        val jwt = JWT.require(Algorithm.RSA256(pair.public as RSAPublicKey, null))
                            .withIssuer("books@test.iam.gserviceaccount.com")
                            .withAudience("https://oauth2.googleapis.com/token").build()
                            .verify(form["assertion"])
                        assertEquals("https://www.googleapis.com/auth/androidpublisher", jwt.getClaim("scope").asString())
                        assertEquals(3600L, jwt.expiresAt.toInstant().epochSecond - jwt.issuedAt.toInstant().epochSecond)
                        exchanges++
                        call.respondText("""{"access_token":"access-1","expires_in":3600,"token_type":"Bearer"}""", ContentType.Application.Json)
                    }
                }
            }
        }
        try {
            val provider = GoogleServiceAccountToken(client, credentials)
            val tokens = coroutineScope { (1..10).map { async { provider.get() } }.awaitAll() }
            assertTrue(tokens.all { it == "access-1" })
            assertEquals(1, exchanges)
        } finally {
            credentials.delete()
        }
    }
}
