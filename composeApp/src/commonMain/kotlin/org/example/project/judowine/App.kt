package org.example.project.judowine

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.example.project.judowine.ui.screen.profile.ProfileRegistrationScreen
import org.example.project.judowine.domain.usecase.SaveUserProfileUseCase
import org.koin.compose.koinInject

/**
 * Main App composable for EventMeet with Koin Dependency Injection.
 *
 * Architecture Note:
 * - Uses Koin's koinInject() to get Use Cases automatically
 * - Does NOT directly access /data module (maintains layer isolation)
 * - All data operations go through domain Use Cases
 * - Dependencies are resolved at runtime by Koin DI framework
 */
@Composable
@Preview
fun App() {
    // Inject Use Case via Koin
    val saveUserProfileUseCase: SaveUserProfileUseCase = koinInject()

    MaterialTheme {
        // Display Profile Registration Screen
        ProfileRegistrationScreen(
            saveUserProfileUseCase = saveUserProfileUseCase,
            onRegistrationSuccess = {
                // TODO: Navigate to home screen or profile display
                println("Registration successful!")
            },
            modifier = Modifier
        )
    }
}