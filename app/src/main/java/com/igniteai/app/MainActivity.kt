package com.igniteai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import com.igniteai.app.feature.audio.UiSoundManager
import com.igniteai.app.ui.navigation.R2H18NavGraph
import com.igniteai.app.ui.theme.R2H18Theme

/**
 * Single Activity host for the entire app.
 *
 * Uses Jetpack Compose for all UI. Navigation is handled by
 * Compose Navigation (NavGraph added in Task 3).
 *
 * Handles:
 * - Stripe payment deep link returns (igniteai://payment)
 * - Edge-to-edge display
 * - Biometric gate on launch (added in Task 25)
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            R2H18Theme {
                DisposableEffect(Unit) {
                    UiSoundManager.startBackgroundMusic()
                    onDispose {
                        UiSoundManager.stopBackgroundMusic()
                    }
                }
                Surface(modifier = Modifier.fillMaxSize()) {
                    R2H18NavGraph()
                }
            }
        }
    }
}
