package com.spasinnya.domain.usecase

import com.spasinnya.data.repository.database.dto.BooksRoot
import com.spasinnya.domain.port.TransactionRunner
import com.spasinnya.domain.repository.ContentSeedRepository
import kotlinx.serialization.json.Json

class SeedBooksFromJsonUseCase(
    private val tx: TransactionRunner,
    private val repo: ContentSeedRepository,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    suspend fun seedFromString(jsonContent: String, skipHomework: Boolean = false): Result<Unit> = runCatching {
        val root = json.decodeFromString<BooksRoot>(jsonContent)
        tx.inTransaction {
            for (book in root.books) {
                repo.upsertBook(book.id, book.number, book.title, book.subtitle).getOrThrow()

                for (week in book.contents.weeks) {
                    val weekPk = repo.upsertWeek(book.id, week.number, week.title).getOrThrow()

                    for (lesson in week.lessons) {
                        val lessonPk = repo.upsertLesson(weekPk, lesson.number, lesson.title, lesson.quote).getOrThrow()

                        lesson.content?.forEachIndexed { i, block ->
                            repo.insertLessonBlock(lessonPk, i, block.type, block.data).getOrThrow()
                        }

                        lesson.homeWork?.let { hw ->
                            if (!skipHomework) {
                                val hwPk = repo.upsertHomework(lessonPk, hw.id, hw.question).getOrThrow()
                                hw.block?.forEachIndexed { j, b ->
                                    repo.insertHomeworkBlock(hwPk, j, b.component, b.answer, b.text).getOrThrow()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}