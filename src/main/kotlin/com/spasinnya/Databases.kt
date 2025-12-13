package com.spasinnya

import com.spasinnya.data.repository.BookDataRepository
import com.spasinnya.data.repository.ExposedOtpRepository
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
import com.spasinnya.data.service.security.BrevoEmailSender
import com.spasinnya.data.service.security.OtpGenerator
import com.spasinnya.domain.port.EmailSender
import com.spasinnya.domain.port.OtpHasher
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
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
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
    val otpRepository = ExposedOtpRepository()

    val jwtService: TokenService = JwtServiceImpl()
    /*val emailSender: EmailSender = HttpEmailSender(
        client = buildHttpClient(),
        apiBaseUrl = System.getenv("EMAIL_API_BASE_URL"),
        apiKey = System.getenv("EMAIL_API_KEY"),
        fromEmail = System.getenv("EMAIL_FROM_ADDRESS")
    )*/
    val key = System.getenv("BREVO_API_KEY")?.trim().orEmpty()
    require(key.isNotEmpty()) { "BREVO_API_KEY is empty" }

    val brevoSender: EmailSender = BrevoEmailSender(
        client = buildHttpClient(),
        apiKey = key,
        fromEmail = System.getenv("EMAIL_FROM")
    )

    /*val verifyOtp = VerifyOtpUseCase(
        users = userRepository,
        refresh = refreshRepository,
        tokens = jwtService
    )*/
    val requestOtpUseCase = RequestOtpUseCase(
        otps = otpRepository,
        hasher = OtpHasher(pepper = System.getenv("OTP_PEPPER")),
        generator = OtpGenerator(),
        emailSender = brevoSender,
        clock = { Clock.System.now() }
    )
    val verifyEmailOtp = VerifyEmailOtpUseCase(
        users = userRepository,
        refresh = refreshRepository,
        tokens = jwtService,
        otps = otpRepository,
        hasher = OtpHasher(pepper = System.getenv("OTP_PEPPER")),
        clock = { Clock.System.now() },
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
            requestOtpUseCase = requestOtpUseCase,
            verifyOtp = verifyEmailOtp,
            loginUser = login,
            refreshSession = refreshSession,
            logout = logout,
            clock = { Clock.System.now() }
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

fun buildHttpClient(): HttpClient =
    HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                }
            )
        }
    }
