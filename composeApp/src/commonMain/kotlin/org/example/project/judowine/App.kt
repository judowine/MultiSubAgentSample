package org.example.project.judowine

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.example.project.judowine.navigation.AppNavGraph

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
 * - Start: ProfileRegistrationScreen (for new users)
 * - Main features: Events, People, Meeting Records, Profile
 * - All screens are connected via type-safe Routes
 */
@Composable
@Preview
fun App() {
    // Create NavController for managing navigation
    val navController = rememberNavController()

    MaterialTheme {
        // Main navigation graph with all screens
        AppNavGraph(
            navController = navController,
            modifier = Modifier
        )
    }
}