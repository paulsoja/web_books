package com.spasinnya.domain.exception

enum class PurchaseError {
    INVALID_PROOF, WRONG_APPLICATION, WRONG_ACCOUNT, UNKNOWN_PRODUCT, UNSUPPORTED_STORE,
    UNSUPPORTED_ENVIRONMENT, TRANSACTION_ALREADY_CLAIMED, VERIFICATION_UNAVAILABLE
}

class PurchaseException(val error: PurchaseError) : RuntimeException(error.name)
