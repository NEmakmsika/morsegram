package app.morsegram.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.morsegram.AppGraph
import app.morsegram.data.DEMO_SERVER
import app.morsegram.ui.components.AnimatedBackground
import kotlinx.coroutines.launch
import app.morsegram.ui.components.Entrance
import app.morsegram.ui.components.GradientButton
import app.morsegram.ui.components.MorseLogo

@Composable
fun WelcomeScreen(demo: Boolean, onRegister: () -> Unit, onLogin: () -> Unit) {
    val scope = rememberCoroutineScope()

    AnimatedBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Entrance(delayMillis = 0) { MorseLogo(unit = 18.dp) }
                Spacer(Modifier.height(32.dp))
                Entrance(delayMillis = 120) {
                    Text(
                        "Morsegram",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Entrance(delayMillis = 220) {
                    Text(
                        "Переписка, которую читаете только вы.\nСквозное шифрование по протоколу Signal.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Entrance(delayMillis = 360) {
                GradientButton(text = "Создать аккаунт", onClick = onRegister)
            }
            Spacer(Modifier.height(12.dp))
            Entrance(delayMillis = 440) {
                OutlinedButton(
                    onClick = onLogin,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Text("У меня уже есть аккаунт", style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(4.dp))
            // Демо-режим: можно посмотреть приложение, пока сервер не запущен.
            TextButton(onClick = {
                scope.launch { AppGraph.settings.setServer(if (demo) "" else DEMO_SERVER) }
            }) {
                Text(
                    if (demo) "Демо-режим включён · подключиться к серверу" else "Посмотреть без регистрации (демо)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
