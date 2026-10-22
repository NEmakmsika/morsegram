package app.morsegram.server

import io.ktor.http.HttpStatusCode
import java.sql.Connection

/**
 * Регистрация по почте и паролю с подтверждением кодом из письма, вход по паролю.
 * Код живёт 10 минут, на него даётся 5 попыток, отправить новый можно не чаще раза в минуту.
 */
class AuthService(
    private val db: Database,
    private val mailer: Mailer,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun register(request: RegisterRequest) {
        val email = normalizeEmail(request.email)
        checkPassword(request.password)
        val hash = Crypto.hashPassword(request.password)

        val (userId, code) = db.transaction { c ->
            val existing = c.query("SELECT id, verified FROM users WHERE email = ?", email) {
                it.getLong("id") to it.getBoolean("verified")
            }.firstOrNull()

            val userId = when {
                existing == null -> c.insert(
                    "INSERT INTO users (email, password_hash, created_at) VALUES (?, ?, ?)",
                    email, hash, clock(),
                )
                existing.second -> throw ApiException(HttpStatusCode.Conflict, "email_taken")
                else -> existing.first.also { c.update("UPDATE users SET password_hash = ? WHERE id = ?", hash, it) }
            }
            userId to issueCode(c, userId, ignoreCooldown = true)
        }
        mailer.sendCode(email, code)
    }

    fun resend(request: ResendRequest) {
        val email = normalizeEmail(request.email)
        val code = db.transaction { c ->
            val userId = c.query("SELECT id FROM users WHERE email = ? AND verified = FALSE", email) { it.getLong(1) }
                .firstOrNull() ?: return@transaction null
            issueCode(c, userId, ignoreCooldown = false)
        } ?: return
        mailer.sendCode(email, code)
    }

    fun verify(request: VerifyRequest): SessionResponse {
        val email = normalizeEmail(request.email)
        return db.transaction { c ->
            val row = c.query(
                """
                SELECT u.id, e.code_hash, e.expires_at, e.attempts
                FROM users u JOIN email_codes e ON e.user_id = u.id
                WHERE u.email = ?
                """,
                email,
            ) { CodeRow(it.getLong(1), it.getString(2), it.getLong(3), it.getInt(4)) }.firstOrNull()
                ?: throw ApiException(HttpStatusCode.BadRequest, "invalid_code")

            if (row.expiresAt < clock() || row.attempts >= MAX_ATTEMPTS) {
                throw ApiException(HttpStatusCode.BadRequest, "code_expired")
            }
            if (!Crypto.constantTimeEquals(row.codeHash, codeHash(row.userId, request.code.trim()))) {
                c.update("UPDATE email_codes SET attempts = attempts + 1 WHERE user_id = ?", row.userId)
                c.commit()
                throw ApiException(HttpStatusCode.BadRequest, "invalid_code")
            }

            c.update("DELETE FROM email_codes WHERE user_id = ?", row.userId)
            c.update("UPDATE users SET verified = TRUE WHERE id = ?", row.userId)
            createSession(c, row.userId)
        }
    }

    fun login(request: LoginRequest): SessionResponse {
        val email = normalizeEmail(request.email)
        return db.transaction { c ->
            val (userId, hash, verified) = c.query(
                "SELECT id, password_hash, verified FROM users WHERE email = ?",
                email,
            ) { Triple(it.getLong(1), it.getString(2), it.getBoolean(3)) }.firstOrNull()
                ?: throw ApiException(HttpStatusCode.Unauthorized, "invalid_credentials")

            if (!Crypto.checkPassword(request.password, hash)) {
                throw ApiException(HttpStatusCode.Unauthorized, "invalid_credentials")
            }
            if (!verified) throw ApiException(HttpStatusCode.Forbidden, "not_verified")
            createSession(c, userId)
        }
    }

    fun logout(token: String) = db.transaction { c ->
        c.update("DELETE FROM sessions WHERE token_hash = ?", Crypto.sha256(token))
    }

    /** По токену из заголовка Authorization находим пользователя. */
    fun userIdForToken(token: String): Long? = db.transaction { c ->
        c.query("SELECT user_id FROM sessions WHERE token_hash = ?", Crypto.sha256(token)) { it.getLong(1) }
            .firstOrNull()
    }

    private fun issueCode(c: Connection, userId: Long, ignoreCooldown: Boolean): String {
        val now = clock()
        val lastSent = c.query("SELECT sent_at FROM email_codes WHERE user_id = ?", userId) { it.getLong(1) }.firstOrNull()
        if (!ignoreCooldown && lastSent != null && now - lastSent < RESEND_COOLDOWN_MS) {
            throw ApiException(HttpStatusCode.TooManyRequests, "too_soon")
        }
        val code = Crypto.newCode()
        c.update("DELETE FROM email_codes WHERE user_id = ?", userId)
        c.update(
            "INSERT INTO email_codes (user_id, code_hash, expires_at, attempts, sent_at) VALUES (?, ?, ?, 0, ?)",
            userId, codeHash(userId, code), now + CODE_TTL_MS, now,
        )
        return code
    }

    private fun createSession(c: Connection, userId: Long): SessionResponse {
        val token = Crypto.newToken()
        c.update("INSERT INTO sessions (token_hash, user_id, created_at) VALUES (?, ?, ?)", Crypto.sha256(token), userId, clock())
        return SessionResponse(token, UserService.me(c, userId))
    }

    private fun codeHash(userId: Long, code: String) = Crypto.sha256("$userId:$code")

    private data class CodeRow(val userId: Long, val codeHash: String, val expiresAt: Long, val attempts: Int)

    companion object {
        const val CODE_TTL_MS = 10 * 60_000L
        const val RESEND_COOLDOWN_MS = 60_000L
        const val MAX_ATTEMPTS = 5
    }
}
