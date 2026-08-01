package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object UserAnswers : Table("user_answers") {
    val id = uuid("id").autoGenerate()
    val userId = long("user_id")
    val bookId = varchar("book_id", 100)
    val weekNumber = integer("week_number")
    val lessonNumber = integer("lesson_number")
    val questionId = varchar("question_id", 200)

    // Serialized JSON stored as plain TEXT (not json/jsonb), e.g. [{"id":"lord","answer":""}]
    val answerData = text("answer_data")

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)

    init {
        // UNIQUE (user_id, book_id, question_id) -> conflict target for UPSERT
        uniqueIndex("uq_user_answer", userId, bookId, questionId)
        // idx_user_answers_lesson
        index("idx_user_answers_lesson", false, userId, bookId, weekNumber, lessonNumber)
    }
}
