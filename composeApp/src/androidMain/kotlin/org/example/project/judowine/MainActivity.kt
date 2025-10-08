package org.example.project.judowine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

/**
 * MainActivity for EventMeet Android app.
 *
 * Architecture Note:
 * - No manual dependency injection code required
 * - App() composable uses Koin's koinInject() to get dependencies
 * - Maintains clean separation: Activity → Compose UI → Koin DI
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // App() uses Koin internally via koinInject()
            App()
        }
    }
}

// Preview disabled: requires Use Case dependency injection
// @Preview
// @Composable
// fun AppAndroidPreview() {
//     App(saveUserProfileUseCase = ...)
// }