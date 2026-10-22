package app.morsegram.ui.settings

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.morsegram.AppGraph
import app.morsegram.data.ThemeMode
import app.morsegram.data.UserSettings
import app.morsegram.ui.components.SectionTitle
import app.morsegram.ui.theme.BrandGradient
import app.morsegram.ui.theme.Cyan
import app.morsegram.ui.theme.Night
import app.morsegram.ui.theme.NightSurfaceHigh
import app.morsegram.ui.theme.Pink
import app.morsegram.ui.theme.Snow
import app.morsegram.ui.theme.SnowSurfaceHigh
import app.morsegram.ui.theme.Violet
import kotlinx.coroutines.launch

private val Green = Color(0xFF34D399)
private val Orange = Color(0xFFF59E0B)

@Composable
fun ProfileEditScreen(settings: UserSettings, onBack: () -> Unit) {
    SettingsScaffold("Профиль", onBack) {
        Box(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            ProfileForm(settings = settings, buttonText = "Сохранить", onSaved = onBack)
        }
    }
}

@Composable
fun AppearanceScreen(settings: UserSettings, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    SettingsScaffold("Оформление", onBack) {
        SectionTitle("Тема")
        val modes = ThemeMode.entries
        modes.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                pair.forEach { mode ->
                    ThemeCard(
                        mode = mode,
                        selected = settings.themeMode == mode,
                        onClick = { scope.launch { AppGraph.settings.setThemeMode(mode) } },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Text(
            "«Чёрная» — настоящий чёрный фон. На AMOLED-экранах она экономит батарею.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
    }
}

/** Карточка-превью темы: мини-чат в цветах этой темы. */
@Composable
private fun ThemeCard(mode: ThemeMode, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (background, bubble) = when (mode) {
        ThemeMode.LIGHT -> Snow to SnowSurfaceHigh
        ThemeMode.DARK -> Night to NightSurfaceHigh
        ThemeMode.BLACK -> Color.Black to Color(0xFF16181F)
        ThemeMode.SYSTEM -> Night to NightSurfaceHigh
    }
    val borderColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        label = "themeBorder",
    )
    val borderWidth by animateDpAsState(if (selected) 2.5.dp else 1.dp, label = "themeBorderWidth")
    val scale by animateFloatAsState(if (selected) 1f else 0.96f, spring(dampingRatio = 0.5f), label = "themeScale")
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .border(borderWidth, borderColor, shape)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .then(
                    if (mode == ThemeMode.SYSTEM) {
                        Modifier.background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Snow, Night)))
                    } else {
                        Modifier.background(background)
                    }
                )
                .padding(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    Modifier
                        .size(width = 70.dp, height = 18.dp)
                        .background(bubble, RoundedCornerShape(9.dp)),
                )
                Box(
                    Modifier
                        .size(width = 50.dp, height = 18.dp)
                        .background(bubble, RoundedCornerShape(9.dp)),
                )
            }
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(width = 80.dp, height = 20.dp)
                    .background(BrandGradient, RoundedCornerShape(10.dp)),
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .background(BrandGradient, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Check, contentDescription = "Выбрано", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        Text(
            mode.title,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    SettingsScaffold("Шифрование", onBack) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(BrandGradient)
                .padding(20.dp),
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(10.dp))
            Text("Сквозное шифрование", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Сообщения шифруются на твоём телефоне и расшифровываются только у собеседника. Сервер их прочитать не может.",
                color = Color.White.copy(alpha = 0.9f),
            )
        }
        SectionTitle("Как это устроено")
        SettingsGroup {
            SettingsItem(Icons.Filled.Lock, listOf(Green, Cyan), "Протокол Signal", subtitle = "Тот же, что в Signal и WhatsApp")
            SettingsItem(Icons.Filled.Phone, listOf(Cyan, Violet), "Ключи только на телефоне", subtitle = "На сервер уходят лишь открытые ключи")
            SettingsItem(Icons.Filled.Search, listOf(Violet, Pink), "Код безопасности", subtitle = "Сверка с собеседником по QR — скоро")
        }
        SectionTitle("Статус")
        SettingsGroup {
            SettingsItem(
                Icons.Filled.Info,
                listOf(Orange, Pink),
                "Шифрование ещё не включено",
                subtitle = "Подключим после сервера, сейчас сообщения не уходят с телефона",
            )
        }
    }
}

@Composable
fun DevicesScreen(onBack: () -> Unit) {
    SettingsScaffold("Устройства", onBack) {
        SectionTitle("Это устройство")
        SettingsGroup {
            SettingsItem(
                Icons.Filled.Phone,
                listOf(Cyan, Green),
                "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
                subtitle = "Android ${Build.VERSION.RELEASE} · в сети",
            )
        }
        Text(
            "Когда подключим сервер, здесь появятся другие устройства, на которых открыт твой аккаунт, и их можно будет отключить.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
    }
}

@Composable
fun NotificationsScreen(settings: UserSettings, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    SettingsScaffold("Уведомления", onBack) {
        SettingsGroup {
            SettingsSwitch(
                icon = Icons.Filled.Notifications,
                colors = listOf(Orange, Pink),
                title = "Уведомления о сообщениях",
                checked = settings.notifications,
                onCheckedChange = { scope.launch { AppGraph.settings.setNotifications(it) } },
            )
            SettingsSwitch(
                icon = Icons.Filled.Info,
                colors = listOf(Cyan, Violet),
                title = "Показывать текст",
                subtitle = "Если выключить, в уведомлении будет только «Новое сообщение»",
                checked = settings.messagePreview,
                onCheckedChange = { scope.launch { AppGraph.settings.setMessagePreview(it) } },
            )
        }
    }
}
