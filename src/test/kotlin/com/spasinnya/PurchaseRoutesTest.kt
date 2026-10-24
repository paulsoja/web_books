package com.spasinnya

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.spasinnya.domain.usecase.GetEntitlementsUseCase
import com.spasinnya.domain.usecase.GetBooksUseCase
import com.spasinnya.data.repository.BookDataRepository
import com.spasinnya.presentation.routes.bookRoutes
import com.spasinnya.presentation.routes.purchaseRoutes
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import kotlin.test.*

class PurchaseRoutesTest {
    private val db = PurchaseTestDatabase()
    @AfterTest fun closeDatabase() = db.close()

    private val algorithm = Algorithm.HMAC256("purchase-test-secret")
    private fun token(user: String) = JWT.create().withSubject(user).sign(algorithm)

    private fun Application.setup(db: PurchaseTestDatabase, submit: com.spasinnya.domain.usecase.SubmitPurchaseUseCase = db.submit) {
        configureSerialization()
        configureRouting()
        install(Authentication) {
            jwt("test") {
                verifier(JWT.require(algorithm).build())
                validate { JWTPrincipal(it.payload) }
            }
        }
        routing {
            authenticate("test") {
                purchaseRoutes(submit, GetEntitlementsUseCase(db.entitlements))
                bookRoutes(GetBooksUseCase(BookDataRepository(db.database)))
            }
        }
    }

    @Test
    fun `authenticated purchase returns authoritative access and isolates users`() = testApplication {
        application { setup(db) }
        val response = client.post("/purchases/ios") {
            bearerAuth(token("1"))
            contentType(ContentType.Application.Json)
            setBody("""{"signedTransaction":"valid"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(Json.parseToJsonElement("""{"productId":1,"granted":true}"""),
            Json.parseToJsonElement(response.bodyAsText()))
        for ((user, expected) in listOf("1" to "[1]", "2" to "[]")) {
            val access = client.get("/me/entitlements") { bearerAuth(token(user)) }
            assertEquals(HttpStatusCode.OK, access.status)
            assertEquals(Json.parseToJsonElement(expected),
                Json.parseToJsonElement(access.bodyAsText()).jsonObject["products"])
        }
        val replay = client.post("/purchases/ios") {
            bearerAuth(token("2"))
            contentType(ContentType.Application.Json)
            setBody("""{"signedTransaction":"valid"}""")
        }
        assertEquals(HttpStatusCode.Conflict, replay.status)
    }

    @Test
    fun `legacy unverified purchase route is removed`() = testApplication {
        application { setup(db) }
        assertEquals(HttpStatusCode.NotFound, client.post("/books/1/purchase") {
            bearerAuth(token("1"))
        }.status)
        assertEquals(0L, db.countEntitlements())
    }

    @Test
    fun `endpoints require JWT and valid subject`() = testApplication {
        application { setup(db) }
        assertEquals(HttpStatusCode.Unauthorized, client.get("/me/entitlements").status)
        assertEquals(HttpStatusCode.Unauthorized, client.post("/purchases/ios").status)
        assertEquals(HttpStatusCode.Unauthorized, client.post("/purchases/android").status)
        assertEquals(HttpStatusCode.Unauthorized, client.get("/purchases/android/account").status)
        assertEquals(HttpStatusCode.Unauthorized, client.get("/me/entitlements") {
            bearerAuth(token("invalid"))
        }.status)
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    @Test
    fun `Android endpoint uses JWT account identity and rejects client supplied authority`() = testApplication {
        val accountId = com.spasinnya.domain.model.purchase.AndroidPurchaseIdentity.accountId(1)
        val verifier = com.spasinnya.domain.port.StorePurchaseVerifier { proof ->
            assertEquals(com.spasinnya.domain.model.purchase.StorePlatform.ANDROID, proof.platform)
            assertEquals("android-token", proof.value)
            assertEquals(accountId, proof.accountId)
            com.spasinnya.domain.model.purchase.VerifiedPurchase(
                proof.platform, "android.book.1", "android-tx", kotlin.time.Instant.fromEpochMilliseconds(1780000000000),
                com.spasinnya.domain.model.purchase.StoreEnvironment.PRODUCTION, proof.value
            )
        }
        application { setup(db, com.spasinnya.domain.usecase.SubmitPurchaseUseCase(verifier, db.purchases)) }
        val accountResponse = client.get("/purchases/android/account") { bearerAuth(token("1")) }
        assertEquals(HttpStatusCode.OK, accountResponse.status)
        assertEquals(accountId, Json.parseToJsonElement(accountResponse.bodyAsText()).jsonObject["obfuscatedAccountId"]!!.jsonPrimitive.content)
        for (body in listOf("{}", "broken", """{"purchaseToken":"android-token","userId":2}""")) {
            assertEquals(HttpStatusCode.BadRequest, client.post("/purchases/android") {
                bearerAuth(token("1")); contentType(ContentType.Application.Json); setBody(body)
            }.status)
        }
        repeat(2) {
            val response = client.post("/purchases/android") {
                bearerAuth(token("1")); contentType(ContentType.Application.Json)
                setBody("""{"purchaseToken":"android-token"}""")
            }
            assertEquals(HttpStatusCode.OK, response.status, response.bodyAsText())
        }
        assertEquals(1L, db.countPurchases())
        assertEquals(listOf(1L), db.entitlements.findProductIdsByUser(1).getOrThrow())
        assertEquals(emptyList(), db.entitlements.findProductIdsByUser(2).getOrThrow())
    }

    @Test
    fun `invalid proof and malformed request produce expected errors`() = testApplication {
        application { setup(db) }
        for ((body, expected) in listOf(
            """{"signedTransaction":"wrong-app"}""" to HttpStatusCode.BadRequest,
            """{"signedTransaction":"unknown"}""" to HttpStatusCode.UnprocessableEntity,
            """{"signedTransaction":"bad-jws"}""" to HttpStatusCode.BadRequest,
            """{"signedTransaction":"valid","userId":2,"productId":999,"price":0}""" to HttpStatusCode.BadRequest,
            "{}" to HttpStatusCode.BadRequest,
            "broken json" to HttpStatusCode.BadRequest
        )) {
            val response = client.post("/purchases/ios") {
                bearerAuth(token("1"))
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            assertEquals(expected, response.status, response.bodyAsText())
        }
        assertEquals(0L, db.countPurchases())
    }
}
