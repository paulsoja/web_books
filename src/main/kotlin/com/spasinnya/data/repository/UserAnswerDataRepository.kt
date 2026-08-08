package com.spasinnya.data.repository

import com.spasinnya.data.extension.runDb
import com.spasinnya.data.repository.database.dto.StoredAnswerValue
import com.spasinnya.data.repository.database.table.UserAnswers
import com.spasinnya.domain.model.homework.HomeworkAnswer
import com.spasinnya.domain.model.homework.HomeworkValue
import com.spasinnya.domain.model.homework.WeekHomeworkStatus
import com.spasinnya.domain.repository.UserAnswerRepository
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.countDistinct
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class UserAnswerDataRepository(
    private val database: Database
) : UserAnswerRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun upsertLessonAnswers(
        userId: Long,
        bookId: String,
        weekNumber: Int,
        lessonNumber: Int,
        answers: List<HomeworkAnswer>
    ): Result<Unit> = database.runDb {
        answers.forEach { answer ->
            val payload = answer.values
                .map { StoredAnswerValue(id = it.id, answer = it.answer) }
                .let { json.encodeToString(it) }

            UserAnswers.upsert(
                UserAnswers.userId,
                UserAnswers.bookId,
                UserAnswers.questionId,
                onUpdate = {
                    it[UserAnswers.answerData] = payload
                    it[UserAnswers.weekNumber] = weekNumber
                    it[UserAnswers.lessonNumber] = lessonNumber
                    it[UserAnswers.updatedAt] = CurrentTimestamp
                }
            ) {
                it[UserAnswers.userId] = userId
                it[UserAnswers.bookId] = bookId
                it[UserAnswers.weekNumber] = weekNumber
                it[UserAnswers.lessonNumber] = lessonNumber
                it[UserAnswers.questionId] = answer.questionId
                it[UserAnswers.answerData] = payload
            }
        }
    }

    override suspend fun getLessonAnswers(
        userId: Long,
        bookId: String,
        weekNumber: Int,
        lessonNumber: Int
    ): Result<List<HomeworkAnswer>> = database.runDb {
        UserAnswers
            .selectAll()
            .where {
                (UserAnswers.userId eq userId) and
                    (UserAnswers.bookId eq bookId) and
                    (UserAnswers.weekNumber eq weekNumber) and
                    (UserAnswers.lessonNumber eq lessonNumber)
            }
            .map { row ->
                val stored: List<StoredAnswerValue> =
                    json.decodeFromString(row[UserAnswers.answerData])

                HomeworkAnswer(
                    questionId = row[UserAnswers.questionId],
                    values = stored.map { HomeworkValue(id = it.id, answer = it.answer) }
                )
            }
    }

    override suspend fun getLessonsWithAnswers(
        userId: Long,
        bookId: String,
        weekNumber: Int
    ): Result<List<Int>> = database.runDb {
        UserAnswers
            .select(UserAnswers.lessonNumber)
            .where {
                (UserAnswers.userId eq userId) and
                    (UserAnswers.bookId eq bookId) and
                    (UserAnswers.weekNumber eq weekNumber)
            }
            .withDistinct()
            .map { it[UserAnswers.lessonNumber] }
            .sorted()
    }

    override suspend fun getHomeworkStatusByBookId(
        userId: Long,
        bookId: String
    ): Result<List<WeekHomeworkStatus>> = database.runDb {
        val countColumn = UserAnswers.lessonNumber.countDistinct()
        UserAnswers
            .select(UserAnswers.weekNumber, countColumn)
            .where {
                (UserAnswers.userId eq userId) and
                    (UserAnswers.bookId eq bookId)
            }
            .groupBy(UserAnswers.weekNumber)
            .map {
                WeekHomeworkStatus(
                    weekNumber = it[UserAnswers.weekNumber],
                    completed = it[countColumn]
                )
            }
            .sortedBy { it.weekNumber }
    }
}
