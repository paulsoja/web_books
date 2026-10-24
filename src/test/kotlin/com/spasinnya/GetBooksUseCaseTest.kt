package com.spasinnya

import com.spasinnya.domain.model.book.Author
import com.spasinnya.domain.model.book.Book
import com.spasinnya.domain.model.book.BookContent
import com.spasinnya.domain.model.book.BookShort
import com.spasinnya.domain.repository.BookRepository
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

    @Test
    fun `getBooks grants all books to every user without purchases`() = runBlocking {
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
            bookRepository = FakeBookRepository(testBooks)
        )

        val result = useCase.invoke(userId = 123L, language = "ua")

        assertTrue(result.isSuccess)
        val books = result.getOrThrow()
        assertEquals(2, books.size)
        assertTrue(books.all { it.isPurchased })
        val anotherUsersBooks = useCase.invoke(userId = 456L, language = "ua").getOrThrow()
        assertTrue(anotherUsersBooks.all { it.isPurchased })
    }
}
