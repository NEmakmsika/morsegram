package app.morsegram.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import app.morsegram.BuildConfig
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

enum class ThemeMode(val title: String) {
    SYSTEM("Как в системе"),
    LIGHT("Светлая"),
    DARK("Тёмная"),
    BLACK("Чёрная"),
}

const val DEMO_SERVER = "demo"

data class UserSettings(
    val loggedIn: Boolean = false,
    /** Пусто — сервер по умолчанию, [DEMO_SERVER] — демо-режим без сервера. */
    val server: String = "",
    val token: String = "",
    val email: String = "",
    val displayName: String = "",
    val nickname: String = "",
    val bio: String = "",
    val avatarVersion: Long = 0,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notifications: Boolean = true,
    val messagePreview: Boolean = true,
) {
    val demo: Boolean get() = server == DEMO_SERVER
    val serverUrl: String get() = if (server.isBlank() || demo) BuildConfig.DEFAULT_SERVER else server
}

private val Context.dataStore by preferencesDataStore(name = "settings")

/** Настройки и профиль хранятся на телефоне. Фото профиля лежит отдельным файлом. */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val loggedIn = booleanPreferencesKey("logged_in")
        val server = stringPreferencesKey("server")
        val token = stringPreferencesKey("token")
        val email = stringPreferencesKey("email")
        val displayName = stringPreferencesKey("display_name")
        val nickname = stringPreferencesKey("nickname")
        val bio = stringPreferencesKey("bio")
        val avatarVersion = longPreferencesKey("avatar_version")
        val themeMode = stringPreferencesKey("theme_mode")
        val notifications = booleanPreferencesKey("notifications")
        val messagePreview = booleanPreferencesKey("message_preview")
    }

    private val avatarFile get() = File(context.filesDir, "avatar.jpg")

    val settings: Flow<UserSettings> = context.dataStore.data.map { it.toSettings() }

    /** Сохраняем вход. Токен пока лежит в DataStore; в части 4 переедет в Android Keystore. */
    suspend fun logIn(email: String, token: String = "") = context.dataStore.edit {
        it[Keys.loggedIn] = true
        it[Keys.email] = email
        it[Keys.token] = token
    }

    /** Выход стирает всё, кроме выбранного сервера и темы. */
    suspend fun logOut() {
        context.dataStore.edit { prefs ->
            val server = prefs[Keys.server]
            val theme = prefs[Keys.themeMode]
            prefs.clear()
            server?.let { prefs[Keys.server] = it }
            theme?.let { prefs[Keys.themeMode] = it }
        }
        withContext(Dispatchers.IO) { avatarFile.delete() }
    }

    suspend fun setServer(server: String) = context.dataStore.edit { it[Keys.server] = server.trim() }

    suspend fun updateProfile(displayName: String, nickname: String, bio: String) = context.dataStore.edit {
        it[Keys.displayName] = displayName.trim()
        it[Keys.nickname] = nickname.trim().removePrefix("@")
        it[Keys.bio] = bio.trim()
    }

    suspend fun setThemeMode(mode: ThemeMode) = context.dataStore.edit { it[Keys.themeMode] = mode.name }

    suspend fun setNotifications(enabled: Boolean) = context.dataStore.edit { it[Keys.notifications] = enabled }

    suspend fun setMessagePreview(enabled: Boolean) = context.dataStore.edit { it[Keys.messagePreview] = enabled }

    /** Уменьшаем выбранное фото до 512 px и сохраняем копию внутри приложения. */
    suspend fun setAvatar(uri: Uri) {
        withContext(Dispatchers.IO) {
            val bitmap = decodeScaled(uri, maxSize = 512) ?: return@withContext
            avatarFile.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        }
        context.dataStore.edit { it[Keys.avatarVersion] = System.currentTimeMillis() }
    }

    suspend fun removeAvatar() {
        withContext(Dispatchers.IO) { avatarFile.delete() }
        context.dataStore.edit { it[Keys.avatarVersion] = 0 }
    }

    suspend fun loadAvatar(): Bitmap? = withContext(Dispatchers.IO) {
        if (avatarFile.exists()) BitmapFactory.decodeFile(avatarFile.path) else null
    }

    private fun decodeScaled(uri: Uri, maxSize: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxSize && bounds.outHeight / (sample * 2) >= maxSize) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }

    private fun Preferences.toSettings() = UserSettings(
        loggedIn = this[Keys.loggedIn] ?: false,
        server = this[Keys.server].orEmpty(),
        token = this[Keys.token].orEmpty(),
        email = this[Keys.email].orEmpty(),
        displayName = this[Keys.displayName].orEmpty(),
        nickname = this[Keys.nickname].orEmpty(),
        bio = this[Keys.bio].orEmpty(),
        avatarVersion = this[Keys.avatarVersion] ?: 0,
        themeMode = this[Keys.themeMode]?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
            ?: ThemeMode.SYSTEM,
        notifications = this[Keys.notifications] ?: true,
        messagePreview = this[Keys.messagePreview] ?: true,
    )
}
