package app.morsegram.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.morsegram.AppGraph
import app.morsegram.data.FakeChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val chatId: String) : ViewModel() {

    val title: String = AppGraph.people.cached(chatId)?.name ?: FakeChatRepository.title(chatId)
    val messages = FakeChatRepository.messages(chatId)

    /** С настоящим сервером писать можно будет, когда включим шифрование (часть 4). */
    val canSend: Boolean = AppGraph.demo

    private val _typing = MutableStateFlow(false)
    val typing: StateFlow<Boolean> = _typing.asStateFlow()

    private var replyJob: Job? = null

    fun send(text: String) {
        if (text.isBlank()) return
        FakeChatRepository.send(chatId, text.trim())

        // Пока сервера нет, собеседник «отвечает» сам, чтобы было видно анимации.
        replyJob?.cancel()
        replyJob = viewModelScope.launch {
            delay(700)
            _typing.value = true
            delay(1600)
            _typing.value = false
            FakeChatRepository.fakeReply(chatId)
        }
    }
}
