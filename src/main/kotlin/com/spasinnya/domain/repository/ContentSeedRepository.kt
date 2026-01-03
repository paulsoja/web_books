package com.spasinnya.domain.repository

import com.spasinnya.data.repository.database.table.ContentBlockType
import com.spasinnya.data.repository.database.table.HomeworkComponent

interface ContentSeedRepository {
    fun upsertBook(
        id: Long,
        number: String,
        title: String,
        subtitle: String?,
        language: String
    ): Result<Unit>

    fun upsertWeek(
        bookId: Long,
        number: Int,
        title: String
    ): Result<Long>

    fun upsertLesson(
        weekPk: Long,
        number: Int,
        title: String,
        quote: String?
    ): Result<Long>

    fun insertLessonBlock(
        lessonPk: Long,
        order: Int,
        type: ContentBlockType,
        data: String
    ): Result<Unit>

    fun upsertHomework(
        lessonPk: Long,
        extId: Int?,
        question: String?
    ): Result<Long>

    fun insertHomeworkBlock(
        homeworkPk: Long,
        order: Int,
        component: HomeworkComponent,
        answer: String?,
        text: String?
    ): Result<Unit>
}