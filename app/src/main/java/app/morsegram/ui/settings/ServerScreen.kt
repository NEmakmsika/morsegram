package app.morsegram.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.morsegram.AppGraph
import app.morsegram.BuildConfig
import app.morsegram.data.DEMO_SERVER
import app.morsegram.data.UserSettings
import app.morsegram.ui.components.GradientButton
import app.morsegram.ui.components.MorseTextField
import app.morsegram.ui.theme.Cyan
import app.morsegram.ui.theme.Violet
import kotlinx.coroutines.launch

/** Адрес сервера Morsegram. Смена сервера выкидывает из аккаунта: на новом сервере он другой. */
@Composable
fun ServerScreen(settings: UserSettings, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var address by rememberSaveable { mutableStateOf(if (settings.demo) "" else settings.serverUrl) }
    val normalized = address.trim().let { if (it.isNotEmpty() && !it.startsWith("https://")) "https://$it" else it }

    SettingsScaffold("Сервер", onBack) {
        SettingsGroup {
            SettingsItem(
                icon = Icons.Filled.Settings,
                colors = listOf(Violet, Cyan),
                title = if (settings.demo) "Демо-режим" else settings.serverUrl.removePrefix("https://"),
                subtitle = if (settings.demo) "Без сервера: люди и сообщения ненастоящие" else "Сейчас подключено",
            )
        }
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MorseTextField(
                value = address,
                onValueChange = { address = it },
                label = "Адрес сервера",
                icon = Icons.Filled.Info,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            )
            Text(
                "Если сменить сервер, придётся войти заново.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            GradientButton(
                text = "Подключиться",
                enabled = normalized.length > "https://".length && normalized != settings.serverUrl.takeIf { !settings.demo },
                onClick = {
                    scope.launch {
                        AppGraph.account.logout()
                        AppGraph.settings.setServer(if (normalized == BuildConfig.DEFAULT_SERVER) "" else normalized)
                        onBack()
                    }
                },
            )
            if (!settings.demo) {
                TextButton(onClick = {
                    scope.launch {
                        AppGraph.account.logout()
                        AppGraph.settings.setServer(DEMO_SERVER)
                        onBack()
                    }
                }) { Text("Перейти в демо-режим") }
            }
        }
    }
}
