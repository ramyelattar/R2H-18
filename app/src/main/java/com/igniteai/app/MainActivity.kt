package com.igniteai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.igniteai.app.ui.theme.IgniteAITheme

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
            IgniteAITheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // NavGraph will replace this placeholder in Task 3
                    Text("IgniteAI 🔥")
                }
            }
        }
    }
}
