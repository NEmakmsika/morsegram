package app.morsegram.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Заглушка вместо сервера: пользователи, контакты и сообщения живут только в памяти
 * телефона и пропадают при перезапуске. На этапе 3 её заменит настоящий сервер.
 * Id чата совпадает с id собеседника.
 */
object FakeChatRepository {

    private const val MINUTE = 60_000L

    private val start = System.currentTimeMillis()

    /** Все «зарегистрированные» пользователи — по ним работает глобальный поиск. */
    val users = listOf(
        User("anna", "Анна Котова", "anna_k"),
        User("boris", "Борис", "borisdev"),
        User("vika", "Вика", "vika_morse"),
        User("gosha", "Гоша Миронов", "gosha_m"),
        User("dima", "Дима", "dmitry"),
        User("lena", "Лена Арт", "lena_art"),
        User("max", "Макс", "max_morse"),
    )

    private val _contacts = MutableStateFlow(setOf("anna", "boris", "vika", "gosha"))
    val contacts: StateFlow<Set<String>> = _contacts.asStateFlow()

    private val openedChats = MutableStateFlow(emptySet<String>())

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

    val chats: Flow<List<Chat>> = combine(messages, openedChats) { all, opened ->
        val ids = all.map { it.chatId }.toSet() + opened
        ids.mapNotNull { id ->
            val user = user(id) ?: return@mapNotNull null
            val last = all.lastOrNull { it.chatId == id }
            Chat(
                id = id,
                title = user.name,
                lastMessage = last?.text ?: "Нет сообщений",
                lastTime = last?.time ?: 0,
                lastFromMe = last?.fromMe ?: false,
            )
        }.sortedByDescending { it.lastTime }
    }

    fun user(id: String): User? = users.firstOrNull { it.id == id }

    fun title(chatId: String): String = user(chatId)?.name ?: chatId

    /** Ищем по имени и по нику, «@» в начале запроса можно не писать. */
    fun search(query: String): List<User> {
        val q = query.trim().removePrefix("@").lowercase()
        if (q.isEmpty()) return emptyList()
        return users.filter { it.name.lowercase().contains(q) || it.nickname.lowercase().contains(q) }
    }

    fun addContact(userId: String) = _contacts.update { it + userId }

    fun removeContact(userId: String) = _contacts.update { it - userId }

    /** Чат появляется в списке, даже если в нём ещё нет сообщений. */
    fun openChat(userId: String) = openedChats.update { it + userId }

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
