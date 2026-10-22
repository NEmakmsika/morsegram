package app.morsegram.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import app.morsegram.data.UserSettings
import app.morsegram.ui.chats.ChatsTab
import app.morsegram.ui.contacts.ContactsTab
import app.morsegram.ui.settings.SettingsPage
import app.morsegram.ui.settings.SettingsTab

private enum class Tab(val title: String, val icon: ImageVector) {
    CHATS("Чаты", Icons.Filled.Email),
    CONTACTS("Контакты", Icons.Filled.Person),
    SETTINGS("Настройки", Icons.Filled.Settings),
}

/** Главный экран с нижним меню, как в Telegram: чаты, контакты, настройки. */
@Composable
fun MainScreen(
    settings: UserSettings,
    onOpenChat: (userId: String) -> Unit,
    onOpenSettings: (SettingsPage) -> Unit,
    onLogOut: () -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(Tab.CHATS) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.title) },
                    )
                }
            }
        },
    ) { padding ->
        AnimatedContent(
            targetState = tab,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
            transitionSpec = {
                (fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.97f)) togetherWith fadeOut(tween(120))
            },
            label = "tabs",
        ) { current ->
            when (current) {
                Tab.CHATS -> ChatsTab(onOpenChat)
                Tab.CONTACTS -> ContactsTab(onOpenChat)
                Tab.SETTINGS -> SettingsTab(settings, onOpenSettings, onLogOut)
            }
        }
    }
}
