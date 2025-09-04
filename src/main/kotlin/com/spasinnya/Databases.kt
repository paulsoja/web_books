package com.spasinnya

import com.spasinnya.data.repository.RefreshTokenDataRepository
import com.spasinnya.data.repository.UserDataRepository
import com.spasinnya.data.repository.database.db.buildHikariFromEnv
import com.spasinnya.data.repository.database.db.connectAndMigrate
import com.spasinnya.data.repository.database.table.*
import com.spasinnya.data.service.JwtServiceImpl
import com.spasinnya.data.service.security.BcryptPasswordHasher
import com.spasinnya.domain.model.book.Book
import com.spasinnya.domain.port.PasswordHasher
import com.spasinnya.domain.port.TokenService
import com.spasinnya.domain.repository.RefreshTokenRepository
import com.spasinnya.domain.usecase.*
import com.spasinnya.presentation.routes.authRoutes
import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun Application.configureDatabases() {
    val dataSource = buildHikariFromEnv()
    val database = connectAndMigrate(dataSource)

    val passwordHasher: PasswordHasher = BcryptPasswordHasher()

    val userRepository = UserDataRepository(database)
    val refreshRepository: RefreshTokenRepository = RefreshTokenDataRepository(database)

    val jwtService: TokenService = JwtServiceImpl()

    val verifyOtp = VerifyOtpUseCase(
        users = userRepository,
        refresh = refreshRepository,
        tokens = jwtService
    )
    val register = RegisterUserUseCase(users = userRepository, hasher = passwordHasher)
    val refreshSession =
        RefreshSessionUseCase(users = userRepository, refreshRepo = refreshRepository, tokens = jwtService)
    val login = LoginUserUseCase(
        users = userRepository,
        refreshRepo = refreshRepository,
        hasher = passwordHasher,
        tokens = jwtService
    )
    val logout = LogoutUseCase(refreshRepo = refreshRepository)

    routing {
        authRoutes(
            registerUser = register,
            verifyOtp = verifyOtp,
            loginUser = login,
            refreshSession = refreshSession,
            logout = logout
        )
    }

    routing {
        openAPI(path="openapi")
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
    }

    routing {
        get("/health") {
            call.respondText("ok")
        }
    }
}

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
        /*val url = "jdbc:postgresql://dpg-d2b059ndiees73e8ki0g-a:5432/books_app?user=books_app_user&password=WN91wwD2FGnLex6TaV9kXKDkTVLlDJfD"
        val driver = "org.postgresql.Driver"
        val user = "books_app_user"
        val password = "WN91wwD2FGnLex6TaV9kXKDkTVLlDJfD"*/

        Database.connect(
            url = url,
            driver = driver,
            user = user,
            password = password
        )

        transaction {
            SchemaUtils.createMissingTablesAndColumns(Users, Books, LessonContents, Lessons, Weeks)
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
