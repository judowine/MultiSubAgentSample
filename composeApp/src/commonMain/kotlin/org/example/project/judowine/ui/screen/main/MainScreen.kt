package org.example.project.judowine.ui.screen.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.example.project.judowine.domain.usecase.GetUserProfileUseCase
import org.example.project.judowine.navigation.Routes
import org.example.project.judowine.ui.screen.event.EventListScreen
import org.example.project.judowine.ui.screen.event.EventViewModel
import org.example.project.judowine.ui.screen.people.PeopleListScreen
import org.example.project.judowine.ui.screen.people.PeopleListViewModel
import org.example.project.judowine.ui.screen.profile.ProfileDisplayScreen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main Screen with Bottom Navigation
 *
 * Implementation: Bottom Navigation system (PBI-8)
 * Architecture: Page-level component with nested navigation
 *
 * Features:
 * - Bottom Navigation with 3 tabs (Events, People, Profile)
 * - Independent navigation state per tab
 * - Material3 NavigationBar component
 * - Stateless design - navigation handled via NavController
 *
 * Tab Structure:
 * - Events: EventListScreen
 * - People: PeopleListScreen
 * - Profile: ProfileDisplayScreen
 *
 * Navigation Pattern:
 * - Each tab has its own content screen
 * - Deep navigation from tabs handled by parent NavGraph
 * - Back button returns to previous tab or exits app
 *
 * @param rootNavController Navigation controller from parent AppNavGraph for deep navigation
 * @param modifier Optional modifier for the screen
 */
@Composable
fun MainScreen(
    rootNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Local navigation controller for bottom tabs
    val tabNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            MainBottomNavigationBar(
                navController = tabNavController
            )
        },
        modifier = modifier
    ) { paddingValues ->
        MainNavigationHost(
            tabNavController = tabNavController,
            rootNavController = rootNavController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

/**
 * Bottom Navigation Bar for Main Screen
 *
 * Atomic Design: Organism level
 * Architecture: Stateless component receiving NavController
 *
 * Features:
 * - 3 tabs with icons and labels
 * - Highlights selected tab with primaryContainer color
 * - Handles tab switching via NavController
 *
 * @param navController Navigation controller for tab switching
 */
@Composable
private fun MainBottomNavigationBar(
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        MainTab.entries.forEach { tab ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == tab.route } == true

            NavigationBarItem(
                icon = {
                    Text(tab.icon)
                },
                label = {
                    Text(tab.label)
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(tab.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

/**
 * Navigation Host for Main Screen Tabs
 *
 * Architecture: Nested navigation for tab content
 *
 * Features:
 * - Independent navigation per tab
 * - Tab state preserved during tab switches
 * - Deep navigation delegates to rootNavController
 *
 * @param tabNavController Navigation controller for tab switching
 * @param rootNavController Navigation controller for deep navigation
 * @param modifier Optional modifier
 */
@Composable
private fun MainNavigationHost(
    tabNavController: NavHostController,
    rootNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = tabNavController,
        startDestination = Routes.MainEvents.route,
        modifier = modifier
    ) {
        // Events Tab
        composable(Routes.MainEvents.route) {
            val viewModel = koinViewModel<EventViewModel>()
            val getUserProfileUseCase = koinInject<GetUserProfileUseCase>()

            // Get user's nickname from profile using produceState
            val nicknameState by produceState<String?>(initialValue = null) {
                val user = getUserProfileUseCase.getPrimaryUser()
                value = user?.nickname
            }

            // Wait for nickname to be loaded before rendering EventListScreen
            val nickname = nicknameState
            if (nickname != null) {
                EventListScreen(
                    viewModel = viewModel,
                    nickname = nickname,
                    onEventClick = { event ->
                        // Navigate using root controller for deep navigation
                        rootNavController.navigate(Routes.EventDetail(event.eventId).route)
                    }
                )
            } else {
                // Show loading indicator while fetching profile
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // People Tab
        composable(Routes.MainPeople.route) {
            val viewModel = koinViewModel<PeopleListViewModel>()
            PeopleListScreen(
                viewModel = viewModel,
                onPersonClick = { userId ->
                    // Navigate using root controller for deep navigation
                    rootNavController.navigate(Routes.PersonDetail(userId).route)
                }
            )
        }

        // Profile Tab
        composable(Routes.MainProfile.route) {
            val getUserProfileUseCase = koinInject<GetUserProfileUseCase>()
            ProfileDisplayScreen(
                getUserProfileUseCase = getUserProfileUseCase,
                onEditProfile = {
                    // Navigate using root controller for deep navigation
                    rootNavController.navigate(Routes.ProfileEdit.route)
                }
            )
        }
    }
}

/**
 * Main Tab Definition
 *
 * Domain Model: Tab configuration for bottom navigation
 *
 * Properties:
 * - route: Navigation route for the tab
 * - label: Display label shown in bottom navigation
 * - icon: Emoji icon displayed in bottom navigation
 */
private enum class MainTab(
    val route: String,
    val label: String,
    val icon: String
) {
    Events(Routes.MainEvents.route, "Events", "📅"),
    People(Routes.MainPeople.route, "People", "👥"),
    Profile(Routes.MainProfile.route, "Profile", "👤")
}
