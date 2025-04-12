package com.spasinnya.data.repository.database.entity

import com.spasinnya.data.repository.database.table.Lessons
import com.spasinnya.data.repository.database.table.Weeks
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class WeekEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<WeekEntity>(Weeks)

    var title by Weeks.title
    var number by Weeks.number
    var book by BookEntity referencedOn Weeks.bookId
    val lessons by LessonEntity referrersOn Lessons.weekId
}