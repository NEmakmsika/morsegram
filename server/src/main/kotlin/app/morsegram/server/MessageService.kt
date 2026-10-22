package app.morsegram.server

import io.ktor.http.HttpStatusCode

/**
 * Почтовый ящик для сообщений. Сервер не знает, что внутри: тело — это уже зашифрованные
 * на телефоне байты (base64). Сообщение лежит, пока получатель его не заберёт и не подтвердит.
 */
class MessageService(
    private val db: Database,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun send(senderId: Long, request: SendMessageRequest): Long {
        if (request.body.isEmpty() || request.body.length > MAX_MESSAGE_BODY) {
            throw ApiException(HttpStatusCode.BadRequest, "invalid_body")
        }
        return db.transaction { c ->
            val exists = c.query("SELECT 1 FROM users WHERE id = ? AND verified = TRUE", request.to) { true }.isNotEmpty()
            if (!exists) throw ApiException(HttpStatusCode.NotFound, "user_not_found")
            c.insert(
                "INSERT INTO messages (sender_id, recipient_id, body, created_at) VALUES (?, ?, ?, ?)",
                senderId, request.to, request.body, clock(),
            )
        }
    }

    fun pending(userId: Long): List<MessageResponse> = db.transaction { c ->
        c.query(
            "SELECT id, sender_id, body, created_at FROM messages WHERE recipient_id = ? ORDER BY id LIMIT 500",
            userId,
        ) { MessageResponse(it.getLong(1), it.getLong(2), it.getString(3), it.getLong(4)) }
    }

    /** Получатель забрал сообщения — удаляем их с сервера. */
    fun ack(userId: Long, ids: List<Long>) = db.transaction { c ->
        ids.take(500).forEach { id -> c.update("DELETE FROM messages WHERE id = ? AND recipient_id = ?", id, userId) }
    }
}
