package com.spasinnya.domain.usecase

import com.spasinnya.domain.repository.BookRepository
import com.spasinnya.domain.repository.PurchaseRepository

class PurchaseBookSimpleUseCase(
    private val books: BookRepository,
    private val purchases: PurchaseRepository
) {
    suspend operator fun invoke(userId: Long, bookId: Long): Result<Unit> = runCatching {
        val exists = books.exists(bookId).getOrThrow()
        require(exists) { "Book not found" }

        purchases.markPurchased(userId, bookId).getOrThrow()
    }
}