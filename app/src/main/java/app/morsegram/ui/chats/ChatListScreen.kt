package app.morsegram.ui.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.morsegram.data.FakeChatRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(onChatClick: (chatId: String) -> Unit) {
    val chats by FakeChatRepository.chats.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(topBar = { TopAppBar(title = { Text("Чаты") }) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            items(chats, key = { it.id }) { chat ->
                ListItem(
                    headlineContent = { Text(chat.title) },
                    supportingContent = {
                        Text(chat.lastMessage, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    modifier = Modifier.clickable { onChatClick(chat.id) },
                )
                HorizontalDivider()
            }
        }
    }
}
