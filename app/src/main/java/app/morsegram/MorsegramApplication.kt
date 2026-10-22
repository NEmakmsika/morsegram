package app.morsegram

import android.app.Application
import app.morsegram.data.AccountRepository
import app.morsegram.data.DemoAccountRepository
import app.morsegram.data.DemoPeopleRepository
import app.morsegram.data.PeopleRepository
import app.morsegram.data.RemoteAccountRepository
import app.morsegram.data.RemotePeopleRepository
import app.morsegram.data.SettingsRepository
import app.morsegram.data.UserSettings
import app.morsegram.data.remote.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MorsegramApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.init(SettingsRepository(this))
    }
}

/**
 * Простейший «склад» общих объектов приложения. В демо-режиме отдаёт заглушки,
 * иначе — классы, которые ходят на сервер. При смене сервера всё пересоздаётся.
 */
object AppGraph {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var settings: SettingsRepository
        private set

    @Volatile
    var current: UserSettings = UserSettings()
        private set

    private var builtFor: String? = null
    private lateinit var accountImpl: AccountRepository
    private lateinit var peopleImpl: PeopleRepository

    val demo: Boolean get() = current.demo

    val account: AccountRepository get() = ensure().let { accountImpl }
    val people: PeopleRepository get() = ensure().let { peopleImpl }

    fun init(settings: SettingsRepository) {
        this.settings = settings
        scope.launch { settings.settings.collect { current = it } }
    }

    /** Вызываем, как только прочитали настройки, чтобы не ждать фоновую подписку. */
    fun update(settings: UserSettings) {
        current = settings
    }

    @Synchronized
    private fun ensure() {
        val key = if (current.demo) "demo" else current.serverUrl
        if (key == builtFor) return
        if (current.demo) {
            accountImpl = DemoAccountRepository(settings)
            peopleImpl = DemoPeopleRepository()
        } else {
            val api = ApiClient(current.serverUrl, token = { current.token.ifBlank { null } })
            accountImpl = RemoteAccountRepository(api, settings)
            peopleImpl = RemotePeopleRepository(api)
        }
        builtFor = key
    }
}
