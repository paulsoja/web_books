package com.spasinnya

import com.spasinnya.data.service.purchase.GooglePlayHttpApi
import com.spasinnya.domain.exception.PurchaseError
import com.spasinnya.domain.exception.PurchaseException
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*

class GooglePlayHttpApiTest {
    @Test fun `Google HTTP adapter uses package token authorization and acknowledgement contract`() = testApplication {
        var acknowledged = false
        externalServices {
            hosts("https://androidpublisher.googleapis.com") {
                routing {
                    get("/androidpublisher/v3/applications/test.books/purchases/productsv2/tokens/token-1") {
                        assertEquals("Bearer oauth-token", call.request.headers[HttpHeaders.Authorization])
                        call.respondText("""{"purchaseStateContext":{"purchaseState":"PURCHASED"},"unknownFutureField":true}""", ContentType.Application.Json)
                    }
                    post("/androidpublisher/v3/applications/test.books/purchases/products/book.1/tokens/token-1:acknowledge") {
                        assertEquals("Bearer oauth-token", call.request.headers[HttpHeaders.Authorization])
                        acknowledged = true
                        call.respond(HttpStatusCode.NoContent)
                    }
                }
            }
        }
        val api = GooglePlayHttpApi(client, "test.books") { "oauth-token" }
        assertEquals("PURCHASED", api.getPurchase("token-1").purchaseStateContext.purchaseState)
        api.acknowledge("book.1", "token-1")
        assertTrue(acknowledged)
    }

    @Test fun `Google errors are sanitized and permission or outage errors remain retryable`() = testApplication {
        externalServices {
            hosts("https://androidpublisher.googleapis.com") {
                routing {
                    get("/androidpublisher/v3/applications/test.books/purchases/productsv2/tokens/{status}") {
                        call.respond(HttpStatusCode.fromValue(call.parameters["status"]!!.toInt()), "secret-token-and-provider-details")
                    }
                }
            }
        }
        val api = GooglePlayHttpApi(client, "test.books") { "oauth-token" }
        for (status in listOf(400, 404, 410, 401, 403, 429, 500, 503)) {
            val error = assertFailsWith<PurchaseException> { api.getPurchase(status.toString()) }
            assertEquals(if (status in listOf(400, 404, 410)) PurchaseError.INVALID_PROOF else PurchaseError.VERIFICATION_UNAVAILABLE, error.error)
            assertFalse(error.message!!.contains("secret"))
        }
    }
}
