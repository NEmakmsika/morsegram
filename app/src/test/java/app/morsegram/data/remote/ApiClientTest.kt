package app.morsegram.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class ApiClientTest {

    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    @Test
    fun loginSendsJsonAndReadsSession() = runTest {
        var seenPath = ""
        val engine = MockEngine { request ->
            seenPath = request.url.encodedPath
            respond(
                """{"token":"t1","user":{"id":7,"email":"a@b.cd","displayName":"Анна","nickname":"anna_k","bio":""}}""",
                HttpStatusCode.OK,
                json,
            )
        }
        val api = ApiClient("https://example.test/", token = { null }, engine = engine)

        val session = api.login("a@b.cd", "secret-password")

        assertEquals("/auth/login", seenPath)
        assertEquals("t1", session.token)
        assertEquals("anna_k", session.user.nickname)
    }

    @Test
    fun serverErrorBecomesApiException() = runTest {
        val engine = MockEngine { respond("""{"error":"nickname_taken"}""", HttpStatusCode.Conflict, json) }
        val api = ApiClient("https://example.test", token = { "abc" }, engine = engine)

        try {
            api.updateProfile(ProfileRequest("Анна", "anna_k"))
            fail("должна быть ошибка")
        } catch (e: ApiException) {
            assertEquals("nickname_taken", e.code)
        }
    }

    @Test
    fun sendsBearerToken() = runTest {
        var auth: String? = null
        val engine = MockEngine { request ->
            auth = request.headers[HttpHeaders.Authorization]
            respond("[]", HttpStatusCode.OK, json)
        }
        val api = ApiClient("https://example.test", token = { "abc" }, engine = engine)

        api.contacts()

        assertEquals("Bearer abc", auth)
    }
}
