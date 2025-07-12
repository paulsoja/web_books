package com.spasinnya.data.repository.database.entity

import com.spasinnya.data.repository.database.table.Books
import com.spasinnya.data.repository.database.table.Weeks
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class BookEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BookEntity>(Books)

    var number by Books.number
    var title by Books.title
    var subtitle by Books.subtitle
    val weeks by WeekEntity referrersOn Weeks.bookId
}