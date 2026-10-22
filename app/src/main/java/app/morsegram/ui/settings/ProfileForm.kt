package app.morsegram.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import app.morsegram.AppGraph
import app.morsegram.data.UserSettings
import app.morsegram.ui.auth.isValidNickname
import app.morsegram.ui.components.GradientButton
import app.morsegram.ui.components.MorseTextField
import app.morsegram.ui.components.ProfileAvatar
import app.morsegram.ui.theme.BrandGradient
import kotlinx.coroutines.launch

/** Фото, имя, ник и «о себе». Используется и при регистрации, и в настройках. */
@Composable
fun ProfileForm(
    settings: UserSettings,
    buttonText: String,
    onSaved: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf(settings.displayName) }
    var nickname by rememberSaveable { mutableStateOf(settings.nickname) }
    var bio by rememberSaveable { mutableStateOf(settings.bio) }
    var saving by rememberSaveable { mutableStateOf(false) }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch { AppGraph.settings.setAvatar(uri) }
    }

    val nicknameError = if (nickname.isNotEmpty() && !isValidNickname(nickname)) {
        "Латиница, цифры и _, от 5 до 32 символов, начинается с буквы"
    } else {
        null
    }
    val canSave = name.isNotBlank() && isValidNickname(nickname)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        val interaction = remember { MutableInteractionSource() }
        val pressed by interaction.collectIsPressedAsState()
        val scale by animateFloatAsState(if (pressed) 0.92f else 1f, spring(dampingRatio = 0.45f), label = "avatarScale")

        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable(interaction, indication = null) {
                    pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
        ) {
            ProfileAvatar(name.ifBlank { "?" }, settings.avatarVersion, size = 112.dp)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(34.dp)
                    .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                    .background(BrandGradient, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Выбрать фото", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        if (settings.avatarVersion != 0L) {
            TextButton(onClick = { scope.launch { AppGraph.settings.removeAvatar() } }) {
                Text("Удалить фото")
            }
        }

        MorseTextField(
            value = name,
            onValueChange = { name = it.take(64) },
            label = "Имя",
            icon = Icons.Filled.Person,
        )
        MorseTextField(
            value = nickname,
            onValueChange = { nickname = it.removePrefix("@").take(32) },
            label = "Ник",
            error = nicknameError,
            trailingIcon = null,
            icon = null,
        )
        Row(Modifier.fillMaxWidth()) {
            Text(
                text = if (nickname.isBlank()) "По нику тебя найдут в поиске" else "Тебя найдут по @$nickname",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        MorseTextField(
            value = bio,
            onValueChange = { bio = it.take(140) },
            label = "О себе",
            singleLine = false,
        )
        GradientButton(
            text = buttonText,
            enabled = canSave,
            loading = saving,
            onClick = {
                saving = true
                scope.launch {
                    AppGraph.settings.updateProfile(name, nickname, bio)
                    saving = false
                    onSaved()
                }
            },
        )
    }
}
