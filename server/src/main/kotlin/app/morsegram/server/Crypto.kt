package app.morsegram.server

import at.favre.lib.crypto.bcrypt.BCrypt
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object Crypto {
    private val random = SecureRandom()

    fun hashPassword(password: String): String =
        BCrypt.withDefaults().hashToString(12, password.toCharArray())

    fun checkPassword(password: String, hash: String): Boolean =
        BCrypt.verifyer().verify(password.toCharArray(), hash).verified

    /** Случайный код из 6 цифр, с ведущими нулями. */
    fun newCode(): String = random.nextInt(1_000_000).toString().padStart(6, '0')

    /** Токен сессии: 32 случайных байта. В базе храним только его хеш. */
    fun newToken(): String {
        val bytes = ByteArray(32).also(random::nextBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }

    fun constantTimeEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(), b.toByteArray())
}
