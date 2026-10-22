package app.morsegram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.morsegram.ui.MorsegramApp
import app.morsegram.ui.theme.MorsegramTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MorsegramTheme {
                MorsegramApp()
            }
        }
    }
}
