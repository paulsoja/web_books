package com.spasinnya.data.repository

import com.spasinnya.data.repository.database.table.*
import com.spasinnya.domain.repository.ContentSeedRepository
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.selectAll

class ExposedContentSeedRepository : ContentSeedRepository {

    override fun upsertBook(
        id: Long,
        number: String,
        title: String,
        subtitle: String?
    ): Result<Unit> = runCatching {
        Books.insertIgnore {
            it[Books.id] = id
            it[Books.number] = number
            it[Books.title] = title
            it[Books.subtitle] = subtitle
        }
    }

    override fun upsertWeek(
        bookId: Long,
        number: Int,
        title: String
    ): Result<Long> = runCatching {
        val inserted = Weeks.insertIgnore {
            it[Weeks.bookId] = bookId
            it[Weeks.number] = number
            it[Weeks.title] = title
        }

        inserted.getOrNull(Weeks.id)
            ?: Weeks
                .selectAll()
                .where { (Weeks.bookId eq bookId) and (Weeks.number eq number) }
                .limit(1)
                .single()[Weeks.id]
    }

    override fun upsertLesson(
        weekPk: Long,
        number: Int,
        title: String,
        quote: String?
    ): Result<Long> = runCatching {
        val inserted = Lessons.insertIgnore {
            it[Lessons.weekId] = weekPk
            it[Lessons.number] = number
            it[Lessons.title] = title
            it[Lessons.quote] = quote
        }

        inserted.getOrNull(Lessons.id)
            ?: Lessons
                .selectAll()
                .where { (Lessons.weekId eq weekPk) and (Lessons.number eq number) }
                .limit(1)
                .single()[Lessons.id]
    }

    override fun insertLessonBlock(
        lessonPk: Long,
        order: Int,
        type: ContentBlockType,
        data: String
    ): Result<Unit> = runCatching {
        LessonBlocks.insertIgnore {
            it[LessonBlocks.lessonId] = lessonPk
            it[LessonBlocks.order] = order
            it[LessonBlocks.blockType] = type
            when (type) {
                ContentBlockType.text -> it[LessonBlocks.dataText] = data
                ContentBlockType.image -> it[LessonBlocks.dataImage] = data
            }
        }
    }

    override fun upsertHomework(
        lessonPk: Long,
        extId: Int?,
        question: String?
    ): Result<Long> = runCatching {
        val inserted = Homeworks.insertIgnore {
            it[Homeworks.lessonId] = lessonPk
            it[Homeworks.extId] = extId
            it[Homeworks.question] = question
        }

        inserted.getOrNull(Homeworks.id)
            ?: Homeworks
                .selectAll()
                .where { Homeworks.lessonId eq lessonPk }
                .limit(1)
                .single()[Homeworks.id]
    }

    override fun insertHomeworkBlock(
        homeworkPk: Long,
        order: Int,
        component: HomeworkComponent,
        answer: String?,
        text: String?
    ): Result<Unit> = runCatching {
        HomeworkBlocks.insertIgnore {
            it[HomeworkBlocks.homeworkId] = homeworkPk
            it[HomeworkBlocks.order] = order
            it[HomeworkBlocks.component] = component
            it[HomeworkBlocks.answer] = answer
            it[HomeworkBlocks.extra] = text
        }
    }
}
