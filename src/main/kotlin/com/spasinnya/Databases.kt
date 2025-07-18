package com.spasinnya

import com.spasinnya.data.repository.database.table.*
import com.spasinnya.domain.model.book.Book
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun init() {
        val url = "jdbc:postgresql://localhost:5432/mentoring_db"
        val driver = "org.postgresql.Driver"
        val user = "postgres"
        val password = "root"

        //val url = "jdbc:postgresql://mentoring_db_user:zjdcYhGc3wmxlPpuxV6N2Y7h5HhxLrRx@dpg-d034v1idbo4c73c9phn0-a/mentoring_db"
        /*val url = "jdbc:postgresql://dpg-d034v1idbo4c73c9phn0-a:5432/mentoring_db?user=mentoring_db_user&password=zjdcYhGc3wmxlPpuxV6N2Y7h5HhxLrRx"
        val driver = "org.postgresql.Driver"
        val user = "mentoring_db_user"
        val password = "zjdcYhGc3wmxlPpuxV6N2Y7h5HhxLrRx"*/

        Database.connect(
            url = url,
            driver = driver,
            user = user,
            password = password
        )

        transaction {
            SchemaUtils.create(Users, Books, LessonContents, Lessons, Weeks)
        }
    }

    fun seedDatabase(books: List<Book>) {
        transaction {
            if (Books.selectAll().empty()) {
                books.forEach { book ->
                    val bookId = Books.insertAndGetId {
                        it[number] = book.number
                        it[title] = book.title
                        it[subtitle] = book.subtitle
                    }

                    book.contents.weeks.forEach { week ->
                        val weekId = Weeks.insertAndGetId {
                            it[number] = number
                            it[this.bookId] = bookId
                            it[number] = week.number
                            it[title] = week.title
                        }

                        week.lessons.forEach { lesson ->
                            val lessonId = Lessons.insertAndGetId {
                                it[this.weekId] = weekId
                                it[number] = lesson.number
                                it[title] = lesson.title
                                it[quote] = lesson.quote
                            }

                            lesson.content.forEach { content ->
                                LessonContents.insert {
                                    it[this.lessonId] = lessonId
                                    it[type] = content.type
                                    it[data] = content.data
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun loadBooksFromJson(): BooksWrapper {
        val jsonString = ClassLoader.getSystemClassLoader().readResourceAsText("books/books_ru.json")
        return json.decodeFromString<BooksWrapper>(jsonString)

        /*val jsonFile = File("src/main/resources/books/books_ru.json")
        val jsonString = jsonFile.readText()
        return json.decodeFromString(jsonString)*/
    }

    fun ClassLoader.readResourceAsText(path: String): String {
        return getResourceAsStream(path)
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: error("Resource '$path' not found in classpath")
    }
}
