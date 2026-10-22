package app.morsegram.server

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

/** Ошибка, которую клиент получит как {"error": "<code>"} с нужным HTTP-статусом. */
class ApiException(val status: HttpStatusCode, val code: String) : RuntimeException(code)

@Serializable
data class ErrorResponse(val error: String)

@Serializable
data class RegisterRequest(val email: String, val password: String)

@Serializable
data class VerifyRequest(val email: String, val code: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class ResendRequest(val email: String)

@Serializable
data class SessionResponse(val token: String, val user: MeResponse)

@Serializable
data class MeResponse(
    val id: Long,
    val email: String,
    val displayName: String,
    val nickname: String?,
    val bio: String,
)

@Serializable
data class ProfileRequest(val displayName: String, val nickname: String, val bio: String = "")

/** Что видят о человеке другие: без почты. */
@Serializable
data class PublicUser(
    val id: Long,
    val displayName: String,
    val nickname: String?,
    val bio: String,
)

@Serializable
data class SendMessageRequest(val to: Long, val body: String)

@Serializable
data class SentResponse(val id: Long)

@Serializable
data class MessageResponse(val id: Long, val from: Long, val body: String, val createdAt: Long)

@Serializable
data class AckRequest(val ids: List<Long>)

@Serializable
data class OkResponse(val ok: Boolean = true)
