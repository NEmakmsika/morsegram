package app.morsegram.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.morsegram.BuildConfig
import app.morsegram.data.UserSettings
import app.morsegram.ui.components.ProfileAvatar
import app.morsegram.ui.components.StaggeredRow
import app.morsegram.ui.theme.Cyan
import app.morsegram.ui.theme.Pink
import app.morsegram.ui.theme.Violet

enum class SettingsPage { PROFILE, APPEARANCE, PRIVACY, DEVICES, NOTIFICATIONS, SERVER }

private val Green = Color(0xFF34D399)
private val Orange = Color(0xFFF59E0B)
private val Red = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(
    settings: UserSettings,
    onOpen: (SettingsPage) -> Unit,
    onLogOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var confirmLogOut by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LargeTopAppBar(title = { Text("Настройки", fontWeight = FontWeight.Bold) }, scrollBehavior = scrollBehavior)
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 24.dp),
        ) {
            item { StaggeredRow(0) { ProfileHeader(settings) } }
            item {
                StaggeredRow(1) {
                    SettingsGroup {
                        SettingsItem(Icons.Filled.Person, listOf(Cyan, Violet), "Профиль", subtitle = "Имя, ник, фото", onClick = { onOpen(SettingsPage.PROFILE) })
                        SettingsItem(Icons.Filled.Star, listOf(Violet, Pink), "Оформление", subtitle = settings.themeMode.title, onClick = { onOpen(SettingsPage.APPEARANCE) })
                    }
                }
            }
            item {
                StaggeredRow(2) {
                    SettingsGroup {
                        SettingsItem(Icons.Filled.Lock, listOf(Green, Cyan), "Конфиденциальность и шифрование", onClick = { onOpen(SettingsPage.PRIVACY) })
                        SettingsItem(Icons.Filled.Notifications, listOf(Orange, Pink), "Уведомления", onClick = { onOpen(SettingsPage.NOTIFICATIONS) })
                        SettingsItem(Icons.Filled.Phone, listOf(Cyan, Green), "Устройства", onClick = { onOpen(SettingsPage.DEVICES) })
                        SettingsItem(
                            icon = Icons.Filled.Settings,
                            colors = listOf(Violet, Cyan),
                            title = "Сервер",
                            subtitle = if (settings.demo) "Демо-режим" else settings.serverUrl.removePrefix("https://"),
                            onClick = { onOpen(SettingsPage.SERVER) },
                        )
                    }
                }
            }
            item {
                StaggeredRow(3) {
                    SettingsGroup {
                        SettingsItem(
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            colors = listOf(Red, Pink),
                            title = "Выйти из аккаунта",
                            onClick = { confirmLogOut = true },
                            trailing = {},
                        )
                    }
                }
            }
            item {
                Text(
                    "Morsegram ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                )
            }
        }
    }

    if (confirmLogOut) {
        AlertDialog(
            onDismissRequest = { confirmLogOut = false },
            title = { Text("Выйти из аккаунта?") },
            text = { Text("Переписка на этом телефоне будет удалена.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogOut = false
                    onLogOut()
                }) { Text("Выйти", color = Red) }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogOut = false }) { Text("Отмена") }
            },
        )
    }
}

@Composable
private fun ProfileHeader(settings: UserSettings) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProfileAvatar(settings.displayName, settings.avatarVersion, size = 96.dp)
        Spacer(Modifier.height(12.dp))
        Text(
            settings.displayName.ifBlank { "Без имени" },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        if (settings.nickname.isNotBlank()) {
            Text("@${settings.nickname}", color = MaterialTheme.colorScheme.primary)
        }
        if (settings.email.isNotBlank()) {
            Text(settings.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (settings.bio.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                settings.bio,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
        }
    }
}
