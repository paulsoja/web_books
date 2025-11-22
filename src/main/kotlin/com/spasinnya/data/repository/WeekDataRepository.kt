package com.spasinnya.data.repository

import com.spasinnya.data.extension.runDb
import com.spasinnya.data.repository.database.table.Weeks
import com.spasinnya.domain.model.book.Week
import com.spasinnya.domain.repository.WeekRepository
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll

class WeekDataRepository(
    private val database: Database
) : WeekRepository {

    override suspend fun getWeeksByBookId(bookId: Long): Result<List<Week>> = database.runDb {
        Weeks
            .selectAll()
            .map {
                Week(
                    id = it[Weeks.id],
                    bookId = it[Weeks.bookId],
                    number = it[Weeks.number],
                    title = it[Weeks.title]
                )
            }
    }
}