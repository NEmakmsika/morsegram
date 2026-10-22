package app.morsegram.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.morsegram.AppGraph

/** Фото профиля, а если его нет — кружок с первой буквой имени. */
@Composable
fun ProfileAvatar(
    name: String,
    avatarVersion: Long,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
) {
    var photo by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(avatarVersion) {
        photo = if (avatarVersion == 0L) null else AppGraph.settings.loadAvatar()?.asImageBitmap()
    }

    val current = photo
    if (current != null) {
        Image(
            bitmap = current,
            contentDescription = "Фото профиля",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape),
        )
    } else {
        Avatar(name.ifBlank { "?" }, modifier, size)
    }
}
