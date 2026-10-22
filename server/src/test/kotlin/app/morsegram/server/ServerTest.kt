package app.morsegram.server

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServerTest {

    /** Вместо настоящей почты запоминаем последний код для каждого адреса. */
    private class FakeMailer : Mailer {
        val codes = mutableMapOf<String, String>()
        override fun sendCode(to: String, code: String) {
            codes[to] = code
        }
    }

    private val mailer = FakeMailer()

    private fun withServer(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) = testApplication {
        val db = Database.connect(
            "jdbc:h2:mem:${UUID.randomUUID()};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
            "sa",
            "",
        )
        db.migrate()
        application { morsegram(db, mailer) }
        val client = createClient { install(ContentNegotiation) { json() } }
        block(client)
    }

    private suspend fun HttpClient.postJson(path: String, body: Any, token: String? = null): HttpResponse = post(path) {
        contentType(ContentType.Application.Json)
        setBody(body)
        token?.let { bearerAuth(it) }
    }

    private suspend fun HttpClient.signUp(email: String, nickname: String, name: String): String {
        assertEquals(HttpStatusCode.OK, postJson("/auth/register", RegisterRequest(email, "secret-password")).status)
        val session = postJson("/auth/verify", VerifyRequest(email, mailer.codes.getValue(email))).body<SessionResponse>()
        val profile = put("/me/profile") {
            contentType(ContentType.Application.Json)
            setBody(ProfileRequest(name, nickname))
            bearerAuth(session.token)
        }
        assertEquals(HttpStatusCode.OK, profile.status)
        return session.token
    }

    @Test
    fun registerVerifyAndLogin() = withServer { client ->
        client.postJson("/auth/register", RegisterRequest("Anna@Example.com", "secret-password"))
        val code = mailer.codes.getValue("anna@example.com")

        // До подтверждения почты войти нельзя.
        val early = client.postJson("/auth/login", LoginRequest("anna@example.com", "secret-password"))
        assertEquals(HttpStatusCode.Forbidden, early.status)

        val session = client.postJson("/auth/verify", VerifyRequest("anna@example.com", code)).body<SessionResponse>()
        assertEquals("anna@example.com", session.user.email)

        val wrong = client.postJson("/auth/login", LoginRequest("anna@example.com", "wrong-password"))
        assertEquals(HttpStatusCode.Unauthorized, wrong.status)

        val login = client.postJson("/auth/login", LoginRequest("anna@example.com", "secret-password"))
        assertEquals(HttpStatusCode.OK, login.status)

        val again = client.postJson("/auth/register", RegisterRequest("anna@example.com", "another-password"))
        assertEquals(HttpStatusCode.Conflict, again.status)
    }

    @Test
    fun codeLocksAfterFiveWrongAttempts() = withServer { client ->
        client.postJson("/auth/register", RegisterRequest("boris@example.com", "secret-password"))
        val code = mailer.codes.getValue("boris@example.com")
        val wrongCode = if (code == "000000") "111111" else "000000"

        repeat(AuthService.MAX_ATTEMPTS) {
            val r = client.postJson("/auth/verify", VerifyRequest("boris@example.com", wrongCode))
            assertEquals(HttpStatusCode.BadRequest, r.status)
        }
        val afterLock = client.postJson("/auth/verify", VerifyRequest("boris@example.com", code))
        assertEquals("code_expired", afterLock.body<ErrorResponse>().error)
    }

    @Test
    fun rejectsShortPasswordAndBadEmail() = withServer { client ->
        val short = client.postJson("/auth/register", RegisterRequest("vika@example.com", "1234"))
        assertEquals("invalid_password", short.body<ErrorResponse>().error)
        val email = client.postJson("/auth/register", RegisterRequest("vika", "secret-password"))
        assertEquals("invalid_email", email.body<ErrorResponse>().error)
    }

    @Test
    fun searchContactsAndMessages() = withServer { client ->
        val anna = client.signUp("anna@example.com", "anna_k", "Анна")
        val max = client.signUp("max@example.com", "max_morse", "Макс")

        // Ник занят — второй раз его взять нельзя, даже в другом регистре.
        val taken = client.put("/me/profile") {
            contentType(ContentType.Application.Json)
            setBody(ProfileRequest("Не Анна", "Anna_K"))
            bearerAuth(max)
        }
        assertEquals(HttpStatusCode.Conflict, taken.status)

        val found = client.get("/users/search?q=@max") { bearerAuth(anna) }.body<List<PublicUser>>()
        assertEquals(listOf("max_morse"), found.map { it.nickname })
        val maxId = found.single().id

        assertEquals(HttpStatusCode.OK, client.put("/contacts/$maxId") { bearerAuth(anna) }.status)
        val contacts = client.get("/contacts") { bearerAuth(anna) }.body<List<PublicUser>>()
        assertEquals(listOf("Макс"), contacts.map { it.displayName })

        client.postJson("/messages", SendMessageRequest(maxId, "c2lnbmFsLWNpcGhlcnRleHQ="), anna)
        val inbox = client.get("/messages") { bearerAuth(max) }.body<List<MessageResponse>>()
        assertEquals(listOf("c2lnbmFsLWNpcGhlcnRleHQ="), inbox.map { it.body })

        client.postJson("/messages/ack", AckRequest(inbox.map { it.id }), max)
        assertTrue(client.get("/messages") { bearerAuth(max) }.body<List<MessageResponse>>().isEmpty())
    }

    @Test
    fun privateRoutesNeedToken() = withServer { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.get("/me").status)
        assertEquals(HttpStatusCode.Unauthorized, client.get("/me") { bearerAuth("nope") }.status)
    }
}
