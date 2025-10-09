package org.example.project.judowine

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.example.project.judowine.domain.usecase.GetUserProfileUseCase
import org.example.project.judowine.navigation.AppNavGraph
import org.example.project.judowine.navigation.Routes
import org.koin.compose.koinInject

/**
 * Main App composable for EventMeet with Navigation.
 *
 * Implementation: Navigation system implementation (PBI-8)
 *
 * Architecture:
 * - Uses Navigation Compose for declarative navigation
 * - All ViewModels injected via Koin within NavGraph
 * - Centralized navigation logic in AppNavGraph
 * - Maintains layer isolation (composeApp → shared → data)
 *
 * Navigation Flow:
 * - Start: Determined by profile registration status
 *   - If profile exists: MainScreen (Events tab)
 *   - If no profile: ProfileRegistrationScreen
 * - Main features: Events, People, Meeting Records, Profile
 * - All screens are connected via type-safe Routes
 */
@Composable
@Preview
fun App() {
    // Create NavController for managing navigation
    val navController = rememberNavController()

    // Check if user profile exists to determine start destination
    val getUserProfileUseCase = koinInject<GetUserProfileUseCase>()
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val hasProfile = getUserProfileUseCase.hasAnyUser()
        startDestination = if (hasProfile) {
            Routes.Main.route  // Profile exists, go to main screen
        } else {
            Routes.ProfileRegistration.route  // No profile, show registration
        }
    }

    MaterialTheme {
        // Show navigation graph only after determining start destination
        startDestination?.let { destination ->
            AppNavGraph(
                navController = navController,
                startDestination = destination,
                modifier = Modifier
            )
        }
    }
}