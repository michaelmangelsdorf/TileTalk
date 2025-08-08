package org.swirlsea.tiletalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.swirlsea.tiletalk.grid.ui.MainScreen
import org.swirlsea.tiletalk.ui.theme.TileTalkTheme

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TileTalkTheme {
                MainScreen()
            }
        }
    }
}