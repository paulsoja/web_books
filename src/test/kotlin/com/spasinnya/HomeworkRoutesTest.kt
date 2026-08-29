package com.spasinnya

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.spasinnya.domain.model.homework.BookHomeworkStatus
import com.spasinnya.domain.model.homework.HomeworkAnswer
import com.spasinnya.domain.model.homework.WeekHomeworkStatus
import com.spasinnya.domain.repository.UserAnswerRepository
import com.spasinnya.domain.usecase.*
import com.spasinnya.presentation.routes.homeworkRoutes
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomeworkRoutesTest {

    private val secret = "test-secret"
    private val issuer = "test-issuer"

    private fun generateToken(userId: Long): String {
        return JWT.create()
            .withIssuer(issuer)
            .withClaim("sub", userId.toString())
            .withExpiresAt(Date(System.currentTimeMillis() + 60000))
            .sign(Algorithm.HMAC256(secret))
    }

    private class FakeUserAnswerRepository(
        var allBooksResult: Result<List<BookHomeworkStatus>> = Result.success(emptyList())
    ) : UserAnswerRepository {
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
        ): Result<List<BookHomeworkStatus>> = allBooksResult
    }

    private fun Application.testModule(repo: UserAnswerRepository) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            })
        }
        install(Authentication) {
            jwt("auth-jwt") {
                realm = "test"
                verifier(
                    JWT.require(Algorithm.HMAC256(secret))
                        .withIssuer(issuer)
                        .build()
                )
                validate { credential ->
                    JWTPrincipal(credential.payload)
                }
            }
        }
        routing {
            authenticate("auth-jwt") {
                homeworkRoutes(
                    saveHomeworkAnswersUseCase = SaveHomeworkAnswersUseCase(repo),
                    getHomeworkAnswersUseCase = GetHomeworkAnswersUseCase(repo),
                    getLessonsWithAnswersUseCase = GetLessonsWithAnswersUseCase(repo),
                    getBookHomeworkStatusUseCase = GetBookHomeworkStatusUseCase(repo),
                    getAllBooksHomeworkStatusUseCase = GetAllBooksHomeworkStatusUseCase(repo)
                )
            }
        }
    }

    @Test
    fun `GET homework requires authentication`() = testApplication {
        val repo = FakeUserAnswerRepository()
        application { testModule(repo) }

        val response = client.get("/homework")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET homework returns 200 with list of BookHomeworkStatus when authenticated`() = testApplication {
        val expected = listOf(
            BookHomeworkStatus(bookId = "book-1", completedLessons = 5),
            BookHomeworkStatus(bookId = "book-2", completedLessons = 10)
        )
        val repo = FakeUserAnswerRepository(allBooksResult = Result.success(expected))
        application { testModule(repo) }

        val token = generateToken(userId = 123L)
        val response = client.get("/homework") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains(""""bookId":"book-1""""))
        assertTrue(responseBody.contains(""""completedLessons":5"""))
        assertTrue(responseBody.contains(""""bookId":"book-2""""))
        assertTrue(responseBody.contains(""""completedLessons":10"""))
    }

    @Test
    fun `GET homework returns 500 when repository fails`() = testApplication {
        val repo = FakeUserAnswerRepository(allBooksResult = Result.failure(RuntimeException("DB failure")))
        application { testModule(repo) }

        val token = generateToken(userId = 123L)
        val response = client.get("/homework") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }
}
