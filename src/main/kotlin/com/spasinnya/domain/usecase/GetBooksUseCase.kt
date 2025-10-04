package com.spasinnya.domain.usecase

import com.spasinnya.domain.model.book.BookShort
import com.spasinnya.domain.repository.BookRepository
import com.spasinnya.domain.repository.PurchaseRepository

class GetBooksUseCase(
    private val bookRepository: BookRepository,
    private val purchaseRepository: PurchaseRepository,
) {
    suspend fun invoke(userId: Long): Result<List<BookShort>> = runCatching {
        val books = bookRepository.getAllBooksWithContent().getOrThrow()
        val purchasedIds = purchaseRepository.findBookIdsByUser(userId).getOrThrow().toSet()

        books.map { book ->
            BookShort(
                id = book.id,
                number = book.number,
                title = book.title,
                subtitle = book.subtitle,
                isPurchased = book.id in purchasedIds
            )
        }
    }
}