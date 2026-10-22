package app.morsegram

import android.app.Application
import app.morsegram.data.SettingsRepository

class MorsegramApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.settings = SettingsRepository(this)
    }
}

/** Простейший «склад» общих объектов приложения. */
object AppGraph {
    lateinit var settings: SettingsRepository
}
