package app.morsegram.data

data class Chat(
    val id: String,
    val title: String,
    val lastMessage: String,
)

data class Message(
    val id: Long,
    val chatId: String,
    val text: String,
    val fromMe: Boolean,
)
