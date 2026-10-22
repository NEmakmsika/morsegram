package app.morsegram.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Заглушка вместо сервера: чаты и сообщения живут только в памяти телефона
 * и пропадают при перезапуске. На этапе 3 её заменит настоящий сервер.
 */
object FakeChatRepository {

    private const val MINUTE = 60_000L

    private val start = System.currentTimeMillis()

    private val titles = mapOf(
        "anna" to "Анна",
        "boris" to "Борис",
        "vika" to "Вика",
        "gosha" to "Гоша",
    )

    private val messages = MutableStateFlow(
        listOf(
            Message(1, "gosha", "Скинь потом ссылку на APK", fromMe = false, time = start - 300 * MINUTE),
            Message(2, "vika", "Это правда как Signal?", fromMe = false, time = start - 120 * MINUTE),
            Message(3, "vika", "Да, тот же протокол 🔐", fromMe = true, time = start - 118 * MINUTE),
            Message(4, "boris", "Скоро тут будет шифрование", fromMe = false, time = start - 45 * MINUTE),
            Message(5, "anna", "Привет! Это тестовый чат.", fromMe = false, time = start - 3 * MINUTE),
            Message(6, "anna", "Привет, вижу 👋", fromMe = true, time = start - 2 * MINUTE),
        )
    )

    private val replies = listOf(
        "Ого, работает! 🔥",
        "Принято 👌",
        "Morsegram выглядит круто",
        "А шифрование когда?",
        "Ахах 😄",
    )

    val chats: Flow<List<Chat>> = messages.map { all ->
        titles.mapNotNull { (id, title) ->
            val last = all.lastOrNull { it.chatId == id } ?: return@mapNotNull null
            Chat(id, title, last.text, last.time, last.fromMe)
        }.sortedByDescending { it.lastTime }
    }

    fun title(chatId: String): String = titles[chatId] ?: chatId

    fun messages(chatId: String): Flow<List<Message>> =
        messages.map { all -> all.filter { it.chatId == chatId } }

    fun send(chatId: String, text: String) = add(chatId, text, fromMe = true)

    /** Притворяемся собеседником и отвечаем одной из заготовленных фраз. */
    fun fakeReply(chatId: String) {
        val reply = replies[messages.value.size % replies.size]
        add(chatId, reply, fromMe = false)
    }

    private fun add(chatId: String, text: String, fromMe: Boolean) {
        messages.update { all ->
            all + Message(
                id = (all.maxOfOrNull { it.id } ?: 0) + 1,
                chatId = chatId,
                text = text,
                fromMe = fromMe,
                time = System.currentTimeMillis(),
            )
        }
    }
}
