package com.spasinnya.data.repository

import com.spasinnya.data.extension.runDb
import com.spasinnya.data.repository.database.table.ContentBlockType
import com.spasinnya.data.repository.database.table.LessonBlocks
import com.spasinnya.data.repository.database.table.Lessons
import com.spasinnya.domain.model.book.Lesson
import com.spasinnya.domain.model.book.LessonContent
import com.spasinnya.domain.repository.LessonRepository
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll

class LessonDataRepository(
    private val database: Database
) : LessonRepository {

    override suspend fun getLessonsByWeekId(weekId: Long): Result<List<Lesson>> = database.runDb {
        val rows = Lessons
            .join(
                otherTable = LessonBlocks,
                joinType = JoinType.INNER,
                additionalConstraint = { LessonBlocks.lessonId eq Lessons.id }
            )
            .selectAll()
            .where { Lessons.weekId eq weekId }
            .orderBy(
                Lessons.number to SortOrder.ASC,
                LessonBlocks.order to SortOrder.ASC
            )
            .toList()

        rows
            .groupBy { it[Lessons.id] }
            .values
            .map { lessonRows ->
                val first = lessonRows.first()

                val contents = lessonRows.map { row ->
                    row.toLessonContent()
                }

                Lesson(
                    id = first[Lessons.id],
                    weekId = first[Lessons.weekId],
                    number = first[Lessons.number],
                    title = first[Lessons.title],
                    quote = first[Lessons.quote],
                    content = contents
                )
            }
    }
}

private fun ResultRow.toLessonContent(): LessonContent {
    return when (this[LessonBlocks.blockType]) {
        ContentBlockType.text -> LessonContent(
            type = "text",
            data = this[LessonBlocks.dataText] ?: ""
        )
        ContentBlockType.image -> LessonContent(
            type = "image",
            data = this[LessonBlocks.dataImage] ?: ""
        )
    }
}