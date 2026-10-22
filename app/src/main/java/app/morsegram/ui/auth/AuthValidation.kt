package app.morsegram.ui.auth

const val CODE_LENGTH = 6
const val MIN_PASSWORD_LENGTH = 8

private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

/** Как в Telegram: латиница, цифры и «_», от 5 до 32 символов, начинается с буквы. */
private val nicknameRegex = Regex("^[a-zA-Z][a-zA-Z0-9_]{4,31}$")

fun isValidEmail(email: String): Boolean = emailRegex.matches(email.trim())

fun isValidCode(code: String): Boolean = code.length == CODE_LENGTH && code.all { it.isDigit() }

fun isValidPassword(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

fun isValidNickname(nickname: String): Boolean = nicknameRegex.matches(nickname.trim().removePrefix("@"))
