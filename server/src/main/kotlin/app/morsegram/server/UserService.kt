package app.morsegram.server

import io.ktor.http.HttpStatusCode
import java.sql.Connection
import java.sql.ResultSet

class UserService(private val db: Database) {

    fun me(userId: Long): MeResponse = db.transaction { me(it, userId) }

    fun updateProfile(userId: Long, request: ProfileRequest): MeResponse = db.transaction { c ->
        val name = request.displayName.trim()
        if (name.isEmpty() || name.length > 64) throw ApiException(HttpStatusCode.BadRequest, "invalid_name")
        if (request.bio.length > 140) throw ApiException(HttpStatusCode.BadRequest, "invalid_bio")
        val nickname = normalizeNickname(request.nickname)

        val takenBy = c.query("SELECT id FROM users WHERE nickname_key = ?", nickname.lowercase()) { it.getLong(1) }
            .firstOrNull()
        if (takenBy != null && takenBy != userId) throw ApiException(HttpStatusCode.Conflict, "nickname_taken")

        c.update(
            "UPDATE users SET display_name = ?, nickname = ?, nickname_key = ?, bio = ? WHERE id = ?",
            name, nickname, nickname.lowercase(), request.bio.trim(), userId,
        )
        me(c, userId)
    }

    /** Ищем подтверждённых пользователей по нику и имени. Себя не показываем. */
    fun search(userId: Long, query: String): List<PublicUser> {
        val q = query.trim().removePrefix("@").lowercase()
        if (q.length < 2) return emptyList()
        val pattern = "%" + q.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%"
        return db.transaction { c ->
            c.query(
                """
                SELECT id, display_name, nickname, bio FROM users
                WHERE verified = TRUE AND nickname_key IS NOT NULL AND id <> ?
                  AND (nickname_key LIKE ? ESCAPE '\' OR LOWER(display_name) LIKE ? ESCAPE '\')
                ORDER BY CASE WHEN nickname_key = ? THEN 0 ELSE 1 END, nickname_key
                LIMIT 20
                """,
                userId, pattern, pattern, q,
                map = ::publicUser,
            )
        }
    }

    fun user(id: Long): PublicUser = db.transaction { c ->
        c.query("SELECT id, display_name, nickname, bio FROM users WHERE id = ? AND verified = TRUE", id, map = ::publicUser)
            .firstOrNull() ?: throw ApiException(HttpStatusCode.NotFound, "user_not_found")
    }

    fun contacts(userId: Long): List<PublicUser> = db.transaction { c ->
        c.query(
            """
            SELECT u.id, u.display_name, u.nickname, u.bio
            FROM contacts k JOIN users u ON u.id = k.contact_id
            WHERE k.owner_id = ?
            ORDER BY u.display_name
            """,
            userId,
            map = ::publicUser,
        )
    }

    fun addContact(userId: Long, contactId: Long) = db.transaction { c ->
        if (contactId == userId) throw ApiException(HttpStatusCode.BadRequest, "cannot_add_self")
        val exists = c.query("SELECT 1 FROM users WHERE id = ? AND verified = TRUE", contactId) { true }.isNotEmpty()
        if (!exists) throw ApiException(HttpStatusCode.NotFound, "user_not_found")
        val already = c.query("SELECT 1 FROM contacts WHERE owner_id = ? AND contact_id = ?", userId, contactId) { true }
        if (already.isEmpty()) c.update("INSERT INTO contacts (owner_id, contact_id) VALUES (?, ?)", userId, contactId)
    }

    fun removeContact(userId: Long, contactId: Long) = db.transaction { c ->
        c.update("DELETE FROM contacts WHERE owner_id = ? AND contact_id = ?", userId, contactId)
    }

    companion object {
        fun me(c: Connection, userId: Long): MeResponse =
            c.query("SELECT id, email, display_name, nickname, bio FROM users WHERE id = ?", userId) {
                MeResponse(it.getLong(1), it.getString(2), it.getString(3), it.getString(4), it.getString(5))
            }.first()

        private fun publicUser(rs: ResultSet) = PublicUser(
            id = rs.getLong(1),
            displayName = rs.getString(2),
            nickname = rs.getString(3),
            bio = rs.getString(4),
        )
    }
}
