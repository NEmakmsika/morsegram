package app.morsegram.server

import io.ktor.http.HttpStatusCode

const val MIN_PASSWORD_LENGTH = 8
const val MAX_PASSWORD_LENGTH = 72 // bcrypt учитывает только первые 72 байта
const val MAX_MESSAGE_BODY = 64 * 1024

private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
private val nicknameRegex = Regex("^[a-zA-Z][a-zA-Z0-9_]{4,31}$")

fun normalizeEmail(email: String): String {
    val value = email.trim().lowercase()
    if (value.length > 254 || !emailRegex.matches(value)) throw ApiException(HttpStatusCode.BadRequest, "invalid_email")
    return value
}

fun checkPassword(password: String) {
    if (password.length < MIN_PASSWORD_LENGTH || password.toByteArray().size > MAX_PASSWORD_LENGTH) {
        throw ApiException(HttpStatusCode.BadRequest, "invalid_password")
    }
}

fun normalizeNickname(nickname: String): String {
    val value = nickname.trim().removePrefix("@")
    if (!nicknameRegex.matches(value)) throw ApiException(HttpStatusCode.BadRequest, "invalid_nickname")
    return value
}
