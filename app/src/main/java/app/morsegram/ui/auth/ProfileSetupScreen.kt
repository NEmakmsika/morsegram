package app.morsegram.ui.auth

import androidx.compose.runtime.Composable
import app.morsegram.data.UserSettings
import app.morsegram.ui.settings.ProfileForm

@Composable
fun ProfileSetupScreen(settings: UserSettings, onDone: () -> Unit) {
    AuthForm(
        title = "Профиль",
        subtitle = "Как тебя будут видеть друзья. Всё это можно поменять потом в настройках.",
        onBack = null,
    ) {
        ProfileForm(settings = settings, buttonText = "Готово", onSaved = onDone)
    }
}
