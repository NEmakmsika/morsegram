package app.morsegram.ui.auth

const val CODE_LENGTH = 6

private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

fun isValidEmail(email: String): Boolean = emailRegex.matches(email.trim())

fun isValidCode(code: String): Boolean = code.length == CODE_LENGTH && code.all { it.isDigit() }
