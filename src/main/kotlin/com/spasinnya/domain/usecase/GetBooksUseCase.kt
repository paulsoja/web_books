package com.spasinnya.domain.usecase

import com.spasinnya.domain.model.book.BookShort
import com.spasinnya.domain.repository.BookRepository

class GetBooksUseCase(
    private val bookRepository: BookRepository,
) {
    suspend fun invoke(userId: Long, language: String): Result<List<BookShort>> = runCatching {
        val books = bookRepository.getAllBooksWithContentByLanguage(language).getOrThrow()

        books.map { book ->
            BookShort(
                id = book.id,
                number = book.number,
                title = book.title,
                subtitle = book.subtitle,
                language = book.language,
                // Temporary free access while purchases are not enabled for users.
                isPurchased = true
            )
        }
    }
}
