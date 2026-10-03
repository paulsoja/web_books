package com.spasinnya

import com.spasinnya.domain.model.book.Author
import com.spasinnya.domain.model.book.Book
import com.spasinnya.domain.model.book.BookContent
import com.spasinnya.domain.model.book.BookShort
import com.spasinnya.domain.repository.BookRepository
import com.spasinnya.domain.repository.EntitlementRepository
import com.spasinnya.domain.usecase.GetBooksUseCase
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetBooksUseCaseTest {

    private class FakeBookRepository(
        private val books: List<Book> = emptyList()
    ) : BookRepository {
        override suspend fun getAllBooksWithContentByLanguage(language: String): Result<List<Book>> =
            Result.success(books)

        override suspend fun getBooksByLanguage(language: String): Result<List<BookShort>> =
            Result.success(books.map {
                BookShort(
                    id = it.id,
                    number = it.number,
                    title = it.title,
                    subtitle = it.subtitle,
                    language = it.language,
                    isPurchased = true
                )
            })

        override suspend fun getBookById(bookId: Int): Result<Book> = throw NotImplementedError()

        override suspend fun exists(bookId: Long): Result<Boolean> = Result.success(true)
    }

    private class FakeEntitlementRepository(
        private val productIds: List<Long> = emptyList()
    ) : EntitlementRepository {
        override suspend fun findProductIdsByUser(userId: Long): Result<List<Long>> =
            Result.success(productIds)
    }

    @Test
    fun `getBooks returns all books with isPurchased true by default for all users`() = runBlocking {
        val testBooks = listOf(
            Book(
                id = 1L,
                number = "1",
                title = "Book 1",
                subtitle = null,
                language = "ua",
                contents = BookContent(emptyList(), Author("", "", ""))
            ),
            Book(
                id = 2L,
                number = "2",
                title = "Book 2",
                subtitle = null,
                language = "ua",
                contents = BookContent(emptyList(), Author("", "", ""))
            )
        )

        val useCase = GetBooksUseCase(
            bookRepository = FakeBookRepository(testBooks),
            entitlementRepository = FakeEntitlementRepository(emptyList())
        )

        val result = useCase.invoke(userId = 123L, language = "ua")

        assertTrue(result.isSuccess)
        val books = result.getOrThrow()
        assertEquals(2, books.size)
        assertTrue(books.all { it.isPurchased })
    }
}
