package app.morsegram.ui.auth

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import app.morsegram.ui.components.Entrance
import app.morsegram.ui.components.GradientButton
import app.morsegram.ui.components.MorseTextField
import app.morsegram.ui.components.PasswordField
import app.morsegram.AppGraph
import app.morsegram.data.remote.ApiException
import app.morsegram.ui.errorText
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onNeedsVerification: (email: String) -> Unit,
    onBack: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var checking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun submit() = scope.launch {
        error = null
        checking = true
        val address = email.trim()
        try {
            AppGraph.account.login(address, password)
            checking = false
            onLoggedIn()
        } catch (e: ApiException) {
            checking = false
            if (e.code == "not_verified") {
                // Почта не подтверждена — шлём новый код и ведём на экран кода.
                runCatching { AppGraph.account.resend(address) }
                onNeedsVerification(address)
            } else {
                error = errorText(e)
            }
        } catch (e: Exception) {
            checking = false
            error = errorText(e)
        }
    }

    AuthForm(
        title = "Вход",
        subtitle = "Почта и пароль, которые ты указал при регистрации.",
        onBack = onBack,
    ) {
        Entrance(delayMillis = 160) {
            MorseTextField(
                value = email,
                onValueChange = { email = it },
                label = "Почта",
                icon = Icons.Filled.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
        }
        Entrance(delayMillis = 220) {
            PasswordField(password, { password = it }, "Пароль")
        }
        ErrorMessage(error)
        Entrance(delayMillis = 280) {
            GradientButton(
                text = "Войти",
                onClick = { submit() },
                enabled = isValidEmail(email) && isValidPassword(password),
                loading = checking,
            )
        }
    }
}
