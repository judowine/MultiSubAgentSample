package org.example.project.judowine.ui.screen.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.project.judowine.domain.model.ConnpassUser
import org.example.project.judowine.domain.model.Event
import org.example.project.judowine.openUrl
import org.example.project.judowine.ui.component.molecule.EventCard
import org.example.project.judowine.ui.component.organism.UserInfoHeader
import org.example.project.judowine.ui.component.organism.CommonEventsList

/**
 * User Detail Screen for EventMeet application.
 *
 * Implemented by: compose-ui-architect
 * PBI-3, Task 6.3: UserDetailScreen for viewing user profile and events
 *
 * This screen displays detailed information for a specific connpass user:
 * - User information (icon, display name, nickname, bio)
 * - Social links (Twitter, GitHub, Connpass profile)
 * - Participated events list (scrollable, reuses EventCard)
 * - Common events section (if logged-in user provided)
 *
 * Design Notes:
 * - Follows Screen/Content separation pattern from PBI-1 and PBI-2
 * - Integrates with UserDetailViewModel using MVI pattern
 * - Handles Loading, Success, and Error states
 * - Reuses EventCard component from PBI-2
 * - Stateless components for easy testing and reusability
 * - NO direct data layer access (uses ViewModel only)
 * - Uses Material3 design system for consistency
 *
 * @param viewModel The UserDetailViewModel managing state
 * @param nickname Target user's connpass nickname to display
 * @param loggedInUserNickname Logged-in user's nickname (nullable, for common events detection)
 * @param onNavigateBack Callback to navigate back to previous screen
 * @param onEventClick Callback when an event card is clicked (navigates to EventDetailScreen)
 * @param modifier Optional modifier for customization
 */
@Composable
fun UserDetailScreen(
    viewModel: UserDetailViewModel,
    nickname: String,
    loggedInUserNickname: String? = null,
    onNavigateBack: () -> Unit = {},
    onEventClick: (Event) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Observe UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Load user detail on screen entry
    LaunchedEffect(nickname, loggedInUserNickname) {
        viewModel.handleIntent(
            UserDetailIntent.LoadUserDetail(
                nickname = nickname,
                loggedInUserNickname = loggedInUserNickname
            )
        )
    }

    UserDetailContent(
        state = uiState,
        onNavigateBack = onNavigateBack,
        onEventClick = onEventClick,
        onRetryClick = {
            viewModel.handleIntent(
                UserDetailIntent.LoadUserDetail(
                    nickname = nickname,
                    loggedInUserNickname = loggedInUserNickname
                )
            )
        },
        modifier = modifier
    )
}

/**
 * Stateless content composable for User Detail.
 *
 * This component is designed to be reusable and testable. It receives all state
 * as parameters and emits events through callbacks (stateless pattern).
 *
 * @param state The current detail state (Loading, Success, or Error)
 * @param onNavigateBack Callback when back button is clicked
 * @param onEventClick Callback when an event card is clicked
 * @param onRetryClick Callback when retry button is clicked (in error state)
 * @param modifier Optional modifier for customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailContent(
    state: UserDetailUiState,
    onNavigateBack: () -> Unit,
    onEventClick: (Event) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("User Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            when (state) {
                is UserDetailUiState.Loading -> {
                    LoadingContent()
                }
                is UserDetailUiState.Success -> {
                    UserDetailSuccessContent(
                        user = state.user,
                        events = state.events,
                        commonEvents = state.commonEvents,
                        onEventClick = onEventClick
                    )
                }
                is UserDetailUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetryClick = onRetryClick
                    )
                }
            }
        }
    }
}

/**
 * Loading state content - displays a centered progress indicator.
 */
@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()

            Text(
                text = "Loading user profile...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Success state content - displays full user details with events.
 *
 * Layout structure:
 * - User Info Section (icon, name, bio, social links)
 * - Common Events Section (if available)
 * - Participated Events Section (scrollable list)
 *
 * @param user The ConnpassUser to display
 * @param events List of events user participated in
 * @param commonEvents List of common events (may be empty)
 * @param onEventClick Callback when an event is clicked
 */
@Composable
private fun UserDetailSuccessContent(
    user: ConnpassUser,
    events: List<Event>,
    commonEvents: List<Event>,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Info Header (extracted organism component)
        item {
            UserInfoHeader(
                user = user,
                onConnpassClick = { url ->
                    openUrl(url)
                }
            )
        }

        item {
            HorizontalDivider()
        }

        // Common Events List (extracted organism component)
        if (commonEvents.isNotEmpty()) {
            item {
                CommonEventsList(
                    commonEvents = commonEvents,
                    onEventClick = onEventClick
                )
            }

            item {
                HorizontalDivider()
            }
        }

        // Participated Events Section Header
        item {
            Text(
                text = "Participated Events",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${events.size} event${if (events.size != 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Participated Events List
        if (events.isEmpty()) {
            item {
                EmptyEventsContent()
            }
        } else {
            items(
                items = events,
                key = { event -> event.eventId }
            ) { event ->
                EventCard(
                    event = event,
                    onClick = { onEventClick(event) }
                )
            }
        }
    }
}

// UserInfoSection, SocialLinksSection, and CommonEventsSection have been extracted to reusable components:
// - UserInfoSection + SocialLinksSection -> ui.component.organism.UserInfoHeader
// - CommonEventsSection -> ui.component.organism.CommonEventsList

/**
 * Empty events content - displays message when user has no events.
 */
@Composable
private fun EmptyEventsContent(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📅",
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center
            )

            Text(
                text = "No events found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Text(
                text = "This user hasn't participated in any events yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Error state content - displays error message with retry button.
 *
 * @param message The error message to display
 * @param onRetryClick Callback when retry button is clicked
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .safeContentPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetryClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp)
        ) {
            Text(
                text = "Retry",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
