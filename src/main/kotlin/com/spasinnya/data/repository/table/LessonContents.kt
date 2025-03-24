package com.spasinnya.data.repository.table

import org.jetbrains.exposed.dao.id.IntIdTable

object LessonContents : IntIdTable("lesson_contents") {
    val lessonId = reference("lesson_id", Lessons)
    val type = varchar("type", 50) // text/image
    val data = text("data")
}