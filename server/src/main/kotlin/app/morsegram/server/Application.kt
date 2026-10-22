package app.morsegram.server

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.auth.principal
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.minutes

data class UserSession(val userId: Long, val token: String)

private val AuthLimit = RateLimitName("auth")

fun Application.morsegram(db: Database, mailer: Mailer) {
    val auth = AuthService(db, mailer)
    val users = UserService(db)
    val messages = MessageService(db)
    val log = LoggerFactory.getLogger("Morsegram")

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    install(StatusPages) {
        exception<ApiException> { call, e ->
            call.respond(e.status, ErrorResponse(e.code))
        }
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("bad_request"))
        }
        exception<Throwable> { call, e ->
            log.error("Unhandled error", e)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error"))
        }
    }

    // Защита от подбора паролей и кодов: не больше 10 запросов в минуту с одного адреса.
    install(RateLimit) {
        register(AuthLimit) {
            rateLimiter(limit = 10, refillPeriod = 1.minutes)
            requestKey { call -> call.request.origin.remoteAddress }
        }
    }

    install(Authentication) {
        bearer("session") {
            authenticate { credential ->
                auth.userIdForToken(credential.token)?.let { UserSession(it, credential.token) }
            }
        }
    }

    routing {
        get("/health") { call.respond(OkResponse()) }

        rateLimit(AuthLimit) {
            route("/auth") {
                post("/register") {
                    auth.register(call.receive())
                    call.respond(OkResponse())
                }
                post("/resend") {
                    auth.resend(call.receive())
                    call.respond(OkResponse())
                }
                post("/verify") { call.respond(auth.verify(call.receive())) }
                post("/login") { call.respond(auth.login(call.receive())) }
            }
        }

        authenticate("session") {
            post("/auth/logout") {
                auth.logout(call.session.token)
                call.respond(OkResponse())
            }

            get("/me") { call.respond(users.me(call.session.userId)) }
            put("/me/profile") { call.respond(users.updateProfile(call.session.userId, call.receive())) }

            get("/users/search") {
                call.respond(users.search(call.session.userId, call.request.queryParameters["q"].orEmpty()))
            }
            get("/users/{id}") { call.respond(users.user(call.idParam())) }

            get("/contacts") { call.respond(users.contacts(call.session.userId)) }
            put("/contacts/{id}") {
                users.addContact(call.session.userId, call.idParam())
                call.respond(OkResponse())
            }
            delete("/contacts/{id}") {
                users.removeContact(call.session.userId, call.idParam())
                call.respond(OkResponse())
            }

            post("/messages") {
                val id = messages.send(call.session.userId, call.receive())
                call.respond(SentResponse(id))
            }
            get("/messages") { call.respond(messages.pending(call.session.userId)) }
            post("/messages/ack") {
                messages.ack(call.session.userId, call.receive<AckRequest>().ids)
                call.respond(OkResponse())
            }
        }
    }
}

private val ApplicationCall.session: UserSession
    get() = principal<UserSession>() ?: throw ApiException(HttpStatusCode.Unauthorized, "unauthorized")

private fun ApplicationCall.idParam(): Long =
    parameters["id"]?.toLongOrNull() ?: throw ApiException(HttpStatusCode.BadRequest, "invalid_id")
