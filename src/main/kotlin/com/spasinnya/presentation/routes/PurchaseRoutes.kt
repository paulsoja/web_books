package com.spasinnya.presentation.routes

import com.spasinnya.domain.exception.BadRequestException
import com.spasinnya.domain.exception.UnauthorizedException
import com.spasinnya.domain.model.purchase.PurchaseProof
import com.spasinnya.domain.model.purchase.StorePlatform
import com.spasinnya.domain.usecase.GetEntitlementsUseCase
import com.spasinnya.domain.usecase.SubmitPurchaseUseCase
import com.spasinnya.presentation.model.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.BadRequestException as KtorBadRequestException
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.purchaseRoutes(submitPurchase: SubmitPurchaseUseCase, getEntitlements: GetEntitlementsUseCase) {
    post("/purchases/ios") {
        val userId = call.purchaseUserId()
        val request = try {
            call.receive<IosPurchaseRequest>()
        } catch (_: KtorBadRequestException) {
            throw BadRequestException("Invalid purchase request")
        }
        val productId = submitPurchase(userId, PurchaseProof(StorePlatform.IOS, request.signedTransaction)).getOrThrow()
        call.respond(StorePurchaseResponse(productId, granted = true))
    }
    get("/me/entitlements") {
        call.respond(EntitlementsResponse(getEntitlements(call.purchaseUserId()).getOrThrow()))
    }
}

private fun ApplicationCall.purchaseUserId(): Long =
    principal<JWTPrincipal>()?.payload?.subject?.toLongOrNull()?.takeIf { it > 0 }
        ?: throw UnauthorizedException()
