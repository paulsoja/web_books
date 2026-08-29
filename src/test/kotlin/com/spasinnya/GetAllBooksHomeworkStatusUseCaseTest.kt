package com.spasinnya

import com.spasinnya.domain.model.homework.BookHomeworkStatus
import com.spasinnya.domain.model.homework.HomeworkAnswer
import com.spasinnya.domain.model.homework.WeekHomeworkStatus
import com.spasinnya.domain.repository.UserAnswerRepository
import com.spasinnya.domain.usecase.GetAllBooksHomeworkStatusUseCase
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetAllBooksHomeworkStatusUseCaseTest {

    private class FakeUserAnswerRepository(
        var statusResult: Result<List<BookHomeworkStatus>> = Result.success(emptyList())
    ) : UserAnswerRepository {
        var requestedUserId: Long? = null

        override suspend fun upsertLessonAnswers(
            userId: Long,
            bookId: String,
            weekNumber: Int,
            lessonNumber: Int,
            answers: List<HomeworkAnswer>
        ): Result<Unit> = Result.success(Unit)

        override suspend fun getLessonAnswers(
            userId: Long,
            bookId: String,
            weekNumber: Int,
            lessonNumber: Int
        ): Result<List<HomeworkAnswer>> = Result.success(emptyList())

        override suspend fun getLessonsWithAnswers(
            userId: Long,
            bookId: String,
            weekNumber: Int
        ): Result<List<Int>> = Result.success(emptyList())

        override suspend fun getHomeworkStatusByBookId(
            userId: Long,
            bookId: String
        ): Result<List<WeekHomeworkStatus>> = Result.success(emptyList())

        override suspend fun getAllBooksHomeworkStatus(
            userId: Long
        ): Result<List<BookHomeworkStatus>> {
            requestedUserId = userId
            return statusResult
        }
    }

    @Test
    fun `invoke calls repository with userId and returns success result`() = runBlocking {
        val expected = listOf(
            BookHomeworkStatus(bookId = "1", completedLessons = 5),
            BookHomeworkStatus(bookId = "2", completedLessons = 2)
        )
        val fakeRepo = FakeUserAnswerRepository(statusResult = Result.success(expected))
        val useCase = GetAllBooksHomeworkStatusUseCase(fakeRepo)

        val result = useCase(userId = 42L)

        assertEquals(42L, fakeRepo.requestedUserId)
        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository fails`() = runBlocking {
        val fakeRepo = FakeUserAnswerRepository(
            statusResult = Result.failure(RuntimeException("Database error"))
        )
        val useCase = GetAllBooksHomeworkStatusUseCase(fakeRepo)

        val result = useCase(userId = 42L)

        assertEquals(42L, fakeRepo.requestedUserId)
        assertTrue(result.isFailure)
    }
}
