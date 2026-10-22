package app.morsegram.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.IOException

/** Ошибка от сервера ("email_taken", "invalid_code"...) или "network", если сервер недоступен. */
class ApiException(val code: String) : Exception(code)

class ApiClient(
    private val baseUrl: String,
    private val token: () -> String?,
    engine: HttpClientEngine = OkHttp.create(),
) {
    private val http = HttpClient(engine) {
        expectSuccess = false
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = 20_000
        }
    }

    suspend fun register(email: String, password: String) {
        send<OkResponse>(HttpMethod.Post, "/auth/register") { json(RegisterRequest(email, password)) }
    }

    suspend fun verify(email: String, code: String): SessionResponse =
        send(HttpMethod.Post, "/auth/verify") { json(VerifyRequest(email, code)) }

    suspend fun resend(email: String) {
        send<OkResponse>(HttpMethod.Post, "/auth/resend") { json(ResendRequest(email)) }
    }

    suspend fun login(email: String, password: String): SessionResponse =
        send(HttpMethod.Post, "/auth/login") { json(LoginRequest(email, password)) }

    suspend fun logout() {
        send<OkResponse>(HttpMethod.Post, "/auth/logout")
    }

    suspend fun updateProfile(request: ProfileRequest): MeResponse =
        send(HttpMethod.Put, "/me/profile") { json(request) }

    suspend fun search(query: String): List<PublicUser> =
        send(HttpMethod.Get, "/users/search") { parameter("q", query) }

    suspend fun contacts(): List<PublicUser> = send(HttpMethod.Get, "/contacts")

    suspend fun addContact(id: Long) {
        send<OkResponse>(HttpMethod.Put, "/contacts/$id")
    }

    suspend fun removeContact(id: Long) {
        send<OkResponse>(HttpMethod.Delete, "/contacts/$id")
    }

    private inline fun <reified B : Any> HttpRequestBuilder.json(body: B) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    private suspend inline fun <reified T> send(
        method: HttpMethod,
        path: String,
        crossinline block: HttpRequestBuilder.() -> Unit = {},
    ): T {
        val response: HttpResponse = try {
            http.request(baseUrl.trimEnd('/') + path) {
                this.method = method
                token()?.let { bearerAuth(it) }
                block()
            }
        } catch (e: IOException) {
            throw ApiException("network")
        }
        if (!response.status.isSuccess()) {
            val code = runCatching { response.body<ErrorResponse>().error }.getOrNull()
            throw ApiException(code ?: "http_${response.status.value}")
        }
        return response.body()
    }
}
