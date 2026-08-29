package com.spasinnya

import com.spasinnya.data.repository.UserAnswerDataRepository
import com.spasinnya.data.repository.database.table.UserAnswers
import com.spasinnya.domain.model.homework.HomeworkAnswer
import com.spasinnya.domain.model.homework.HomeworkValue
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserAnswerDataRepositoryTest {

    private lateinit var database: Database
    private lateinit var repository: UserAnswerDataRepository

    @BeforeTest
    fun setup() {
        database = Database.connect(
            url = "jdbc:h2:mem:user_answers_test_${System.currentTimeMillis()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver"
        )
        transaction(database) {
            SchemaUtils.create(UserAnswers)
        }
        repository = UserAnswerDataRepository(database)
    }

    @Test
    fun `getAllBooksHomeworkStatus returns distinct completed lessons count per book`() = runBlocking {
        val userId = 10L

        // User answers 2 questions in Book 1, Week 1, Lesson 1 -> 1 completed lesson
        repository.upsertLessonAnswers(
            userId = userId,
            bookId = "book-1",
            weekNumber = 1,
            lessonNumber = 1,
            answers = listOf(
                HomeworkAnswer("q1", listOf(HomeworkValue("v1", "ans1"))),
                HomeworkAnswer("q2", listOf(HomeworkValue("v2", "ans2")))
            )
        )

        // User answers 1 question in Book 1, Week 1, Lesson 2 -> 2nd completed lesson for Book 1
        repository.upsertLessonAnswers(
            userId = userId,
            bookId = "book-1",
            weekNumber = 1,
            lessonNumber = 2,
            answers = listOf(
                HomeworkAnswer("q3", listOf(HomeworkValue("v3", "ans3")))
            )
        )

        // User answers 1 question in Book 1, Week 2, Lesson 1 -> 3rd completed lesson for Book 1
        repository.upsertLessonAnswers(
            userId = userId,
            bookId = "book-1",
            weekNumber = 2,
            lessonNumber = 1,
            answers = listOf(
                HomeworkAnswer("q4", listOf(HomeworkValue("v4", "ans4")))
            )
        )

        // User answers 1 question in Book 2, Week 1, Lesson 1 -> 1st completed lesson for Book 2
        repository.upsertLessonAnswers(
            userId = userId,
            bookId = "book-2",
            weekNumber = 1,
            lessonNumber = 1,
            answers = listOf(
                HomeworkAnswer("q5", listOf(HomeworkValue("v5", "ans5")))
            )
        )

        // Another user's answers should not affect userId 10
        repository.upsertLessonAnswers(
            userId = 99L,
            bookId = "book-1",
            weekNumber = 1,
            lessonNumber = 3,
            answers = listOf(
                HomeworkAnswer("q6", listOf(HomeworkValue("v6", "ans6")))
            )
        )

        val result = repository.getAllBooksHomeworkStatus(userId)

        assertTrue(result.isSuccess)
        val statuses = result.getOrThrow()
        assertEquals(2, statuses.size)

        assertEquals("book-1", statuses[0].bookId)
        assertEquals(3, statuses[0].completedLessons)

        assertEquals("book-2", statuses[1].bookId)
        assertEquals(1, statuses[1].completedLessons)
    }

    @Test
    fun `getAllBooksHomeworkStatus returns empty list when user has no answers`() = runBlocking {
        val result = repository.getAllBooksHomeworkStatus(userId = 123L)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }
}
