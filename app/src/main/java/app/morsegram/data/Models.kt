package app.morsegram.data

data class User(
    val id: String,
    val name: String,
    val nickname: String,
)

data class Chat(
    val id: String,
    val title: String,
    val lastMessage: String,
    val lastTime: Long,
    val lastFromMe: Boolean,
)

data class Message(
    val id: Long,
    val chatId: String,
    val text: String,
    val fromMe: Boolean,
    val time: Long,
)
