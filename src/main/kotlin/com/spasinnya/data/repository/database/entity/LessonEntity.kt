package com.spasinnya.data.repository.database.entity

import com.spasinnya.data.repository.database.table.Lessons
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class LessonEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<LessonEntity>(Lessons)

    var number by Lessons.number
    var title by Lessons.title
    var quote by Lessons.quote
}