package app.morsegram.ui.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.morsegram.AppGraph
import app.morsegram.data.Chat
import app.morsegram.data.FakeChatRepository
import app.morsegram.ui.components.Avatar
import app.morsegram.ui.components.MorseLogo
import app.morsegram.ui.components.SearchField
import app.morsegram.ui.components.SectionTitle
import app.morsegram.ui.components.StaggeredRow
import app.morsegram.ui.components.UserRow
import app.morsegram.ui.components.rememberPeopleSearch
import app.morsegram.ui.formatTime
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsTab(onOpenChat: (userId: String) -> Unit, modifier: Modifier = Modifier) {
    // С настоящим сервером переписка появится вместе с шифрованием (часть 4).
    val chatsFlow = remember { if (AppGraph.demo) FakeChatRepository.chats else flowOf(emptyList()) }
    val chats by chatsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    var query by rememberSaveable { mutableStateOf("") }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val q = query.trim().removePrefix("@").lowercase()
    val foundChats = if (q.isEmpty()) chats else chats.filter { it.title.lowercase().contains(q) }
    val chatIds = chats.map { it.id }.toSet()
    val globalResults = rememberPeopleSearch(query).filter { it.id !in chatIds }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LargeTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Morsegram", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(12.dp))
                        MorseLogo(unit = 6.dp)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 16.dp),
        ) {
            item(key = "search") {
                SearchField(query, { query = it }, placeholder = "Поиск чатов и людей")
            }
            itemsIndexed(foundChats, key = { _, chat -> "chat-${chat.id}" }) { index, chat ->
                StaggeredRow(index, Modifier.animateItem()) {
                    ChatRow(chat, onClick = { onOpenChat(chat.id) })
                }
            }
            if (globalResults.isNotEmpty()) {
                item(key = "global-title") {
                    SectionTitle("Глобальный поиск", Modifier.animateItem())
                }
                itemsIndexed(globalResults, key = { _, user -> "user-${user.id}" }) { index, user ->
                    StaggeredRow(index, Modifier.animateItem()) {
                        UserRow(user, onClick = { onOpenChat(user.id) })
                    }
                }
            }
            if (q.isEmpty() && chats.isEmpty()) {
                item(key = "no-chats") {
                    Text(
                        "Чатов пока нет. Найди друзей по @нику в поиске выше.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    )
                }
            }
            if (q.isNotEmpty() && foundChats.isEmpty() && globalResults.isEmpty()) {
                item(key = "empty") {
                    Text(
                        "Никого не нашли по «$query»",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatRow(chat: Chat, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(chat.title)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    chat.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (chat.lastTime > 0) {
                    Text(
                        formatTime(chat.lastTime),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = "Зашифровано",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (chat.lastFromMe) "Вы: ${chat.lastMessage}" else chat.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
