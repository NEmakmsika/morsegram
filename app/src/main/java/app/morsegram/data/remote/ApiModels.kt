package app.morsegram.data.remote

import kotlinx.serialization.Serializable

// Те же модели, что на сервере (server/src/main/kotlin/.../Api.kt).

@Serializable
data class ErrorResponse(val error: String)

@Serializable
data class OkResponse(val ok: Boolean = true)

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
    val nickname: String? = null,
    val bio: String = "",
)

@Serializable
data class ProfileRequest(val displayName: String, val nickname: String, val bio: String = "")

@Serializable
data class PublicUser(
    val id: Long,
    val displayName: String,
    val nickname: String? = null,
    val bio: String = "",
)
