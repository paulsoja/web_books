package com.spasinnya

import com.spasinnya.data.repository.BookDataRepository
import com.spasinnya.data.repository.LessonDataRepository
import com.spasinnya.data.repository.PurchaseDataRepository
import com.spasinnya.data.repository.RefreshTokenDataRepository
import com.spasinnya.data.repository.UserDataRepository
import com.spasinnya.data.repository.WeekDataRepository
import com.spasinnya.data.repository.database.db.buildHikariFromEnv
import com.spasinnya.data.repository.database.db.connectAndMigrate
import com.spasinnya.data.repository.database.db.connectFlyway
import com.spasinnya.data.service.ExposedTransactionRunner
import com.spasinnya.data.service.JwtServiceImpl
import com.spasinnya.data.service.security.BcryptPasswordHasher
import com.spasinnya.domain.port.PasswordHasher
import com.spasinnya.domain.port.TokenService
import com.spasinnya.domain.repository.BookRepository
import com.spasinnya.domain.repository.PurchaseRepository
import com.spasinnya.domain.repository.RefreshTokenRepository
import com.spasinnya.domain.repository.UserRepository
import com.spasinnya.domain.repository.WeekRepository
import com.spasinnya.domain.usecase.*
import com.spasinnya.presentation.routes.authRoutes
import com.spasinnya.presentation.routes.bookRoutes
import com.spasinnya.presentation.routes.lessonsRoutes
import com.spasinnya.presentation.routes.userRoutes
import com.spasinnya.presentation.routes.weekRoutes
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureDatabases() {
    val dataSource = buildHikariFromEnv()
    connectFlyway(dataSource)
    val database = connectAndMigrate(dataSource)

    Bootstrap.runSeedAllBooksFromClasspath()

    val passwordHasher: PasswordHasher = BcryptPasswordHasher()

    val userRepository: UserRepository = UserDataRepository(database)
    val refreshRepository: RefreshTokenRepository = RefreshTokenDataRepository(database)
    val bookRepository: BookRepository = BookDataRepository(database)
    val purchaseRepository: PurchaseRepository = PurchaseDataRepository(database)
    val weekRepository: WeekRepository = WeekDataRepository(database)
    val lessonRepository = LessonDataRepository(database)

    val jwtService: TokenService = JwtServiceImpl()

    val verifyOtp = VerifyOtpUseCase(
        users = userRepository,
        refresh = refreshRepository,
        tokens = jwtService
    )
    val register = RegisterUserUseCase(
        users = userRepository,
        hasher = passwordHasher,
        tx = ExposedTransactionRunner()
    )
    val refreshSession = RefreshSessionUseCase(
        users = userRepository,
        refreshRepo = refreshRepository,
        tokens = jwtService
    )
    val login = LoginUserUseCase(
        users = userRepository,
        refreshRepo = refreshRepository,
        hasher = passwordHasher,
        tokens = jwtService
    )
    val logout = LogoutUseCase(refreshRepo = refreshRepository, tokens = jwtService)

    val profileUseCase = GetUserProfileUseCase(userRepository = userRepository)
    val updateUserProfileUseCase = UpdateUserProfileUseCase(
        profiles = userRepository,
        tx = ExposedTransactionRunner()
    )

    val getBooksUseCase = GetBooksUseCase(
        bookRepository = bookRepository,
        purchaseRepository = purchaseRepository
    )

    val purchaseBookSimpleUseCase = PurchaseBookSimpleUseCase(
        books = bookRepository,
        purchases = purchaseRepository
    )

    val getWeeksUseCase = GetWeeksUseCase(weekRepository = weekRepository)

    val getLessonsByWeekIdUseCase = GetLessonsByWeekIdUseCase(lessonRepository = lessonRepository)

    routing {
        authRoutes(
            registerUser = register,
            verifyOtp = verifyOtp,
            loginUser = login,
            refreshSession = refreshSession,
            logout = logout
        )
        authenticate("auth-jwt") {
            userRoutes(
                getUserProfileUseCase = profileUseCase,
                updateUserProfileUseCase = updateUserProfileUseCase
            )
            bookRoutes(
                getBooksUseCase = getBooksUseCase,
                purchaseBookSimpleUseCase = purchaseBookSimpleUseCase
            )
            weekRoutes(getWeeksUseCase = getWeeksUseCase)
            lessonsRoutes(getLessonsByWeekIdUseCase = getLessonsByWeekIdUseCase)
        }
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
