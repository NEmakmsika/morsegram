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

    private val messages = MutableStateFlow(
        listOf(
            Message(1, "anna", "Привет! Это тестовый чат.", fromMe = false),
            Message(2, "anna", "Привет, вижу 👋", fromMe = true),
            Message(3, "boris", "Скоро тут будет шифрование", fromMe = false),
        )
    )

    private val titles = mapOf("anna" to "Анна", "boris" to "Борис")

    val chats: Flow<List<Chat>> = messages.map { all ->
        titles.map { (id, title) ->
            Chat(
                id = id,
                title = title,
                lastMessage = all.lastOrNull { it.chatId == id }?.text.orEmpty(),
            )
        }
    }

    fun title(chatId: String): String = titles[chatId] ?: chatId

    fun messages(chatId: String): Flow<List<Message>> =
        messages.map { all -> all.filter { it.chatId == chatId } }

    fun send(chatId: String, text: String) {
        messages.update { all ->
            all + Message(
                id = (all.maxOfOrNull { it.id } ?: 0) + 1,
                chatId = chatId,
                text = text,
                fromMe = true,
            )
        }
    }
}
