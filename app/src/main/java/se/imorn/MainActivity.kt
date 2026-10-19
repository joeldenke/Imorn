package se.imorn

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import se.imorn.designsystem.theme.ImornTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val graph = (application as ImornApplication).graph
        val viewModelFactory = graph.metroViewModelFactory
        setContent {
            val darkTheme = isSystemInDarkTheme()
            // The system bars follow the app's theme, not the phone's, when the user picked light or dark.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(LightScrim, DarkScrim) { darkTheme },
                )
                onDispose { }
            }
            ImornTheme(darkTheme = darkTheme) {
                // metroViewModel() in each Navigation 3 entry creates its ViewModels with this factory.
                CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory) {
                    ImornApp()
                }
            }
        }
    }

    private companion object {
        // The scrims enableEdgeToEdge() uses by default, for three-button navigation.
        val LightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val DarkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}
