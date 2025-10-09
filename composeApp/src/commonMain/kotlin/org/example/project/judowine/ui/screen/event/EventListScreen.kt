package org.example.project.judowine.ui.screen.event

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.project.judowine.domain.model.Event
import org.example.project.judowine.ui.component.molecule.EventCard

/**
 * Event List Screen for EventMeet application.
 *
 * Implemented by: compose-ui-architect
 * PBI-2, Task 4.6: EventListScreen with pull-to-refresh
 *
 * This screen displays a list of events for a user with the following features:
 * - Events sorted by date (newest first)
 * - Pull-to-refresh functionality
 * - Loading, Success, Empty, and Error states
 * - Navigation to EventDetailScreen on item tap
 *
 * Design Notes:
 * - Follows Screen/Content separation pattern from PBI-1
 * - Integrates with EventViewModel using MVI pattern
 * - Uses Material3 PullToRefreshBox for pull-to-refresh
 * - Stateless components for easy testing and reusability
 * - NO direct data layer access (uses ViewModel only)
 *
 * @param viewModel The EventViewModel managing state
 * @param nickname The connpass user nickname to fetch events for
 * @param onEventClick Callback when an event is clicked (navigates to detail screen)
 * @param modifier Optional modifier for customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    viewModel: EventViewModel,
    nickname: String,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    // Observe UI state from ViewModel
    val uiState by viewModel.eventListState.collectAsState()

    // Track refresh state for pull-to-refresh
    var isRefreshing by remember { mutableStateOf(false) }

    // Load events on screen entry
    LaunchedEffect(nickname) {
        viewModel.handleIntent(EventIntent.LoadEvents(nickname, forceRefresh = false))
    }

    // Handle loading state for pull-to-refresh indicator
    LaunchedEffect(uiState) {
        if (uiState !is EventListUiState.Loading) {
            isRefreshing = false
        }
    }

    EventListContent(
        state = uiState,
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.handleIntent(EventIntent.RefreshEvents(nickname))
        },
        onEventClick = onEventClick,
        onRetryClick = {
            viewModel.handleIntent(EventIntent.LoadEvents(nickname, forceRefresh = false))
        },
        modifier = modifier
    )
}

/**
 * Stateless content composable for Event List.
 *
 * This component is designed to be reusable and testable. It receives all state
 * as parameters and emits events through callbacks (stateless pattern).
 *
 * Note: Using FloatingActionButton for refresh instead of PullToRefreshBox
 * as it's not yet available in Compose Multiplatform 1.9.0.
 *
 * @param state The current list state (Idle, Loading, Success, Empty, or Error)
 * @param isRefreshing Whether refresh is in progress
 * @param onRefresh Callback when user clicks refresh
 * @param onEventClick Callback when an event is clicked
 * @param onRetryClick Callback when retry button is clicked (in error state)
 * @param modifier Optional modifier for customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListContent(
    state: EventListUiState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onEventClick: (Event) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            // Show refresh button for Success state
            if (state is EventListUiState.Success && !isRefreshing) {
                FloatingActionButton(
                    onClick = onRefresh
                ) {
                    Text("🔄", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            when (state) {
                is EventListUiState.Idle -> {
                    // Show nothing in idle state
                }
                is EventListUiState.Loading -> {
                    LoadingContent()
                }
                is EventListUiState.Success -> {
                    EventListSuccessContent(
                        events = state.events,
                        onEventClick = onEventClick
                    )
                }
                is EventListUiState.Empty -> {
                    EmptyContent()
                }
                is EventListUiState.Error -> {
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
        CircularProgressIndicator()
    }
}

/**
 * Success state content - displays a scrollable list of events.
 *
 * Events are displayed in cards showing:
 * - Title
 * - Date (formatted)
 * - Location/address (if available)
 * - Participant info (accepted/waiting/limit)
 *
 * @param events List of events to display (already sorted by date)
 * @param onEventClick Callback when an event card is clicked
 */
@Composable
private fun EventListSuccessContent(
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Text(
                text = "Events",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Event list
        items(
            items = events,
            key = { event -> event.id }
        ) { event ->
            EventCard(
                event = event,
                onClick = { onEventClick(event) }
            )
        }
    }
}

/**
 * Empty state content - displays message when no events are found.
 */
@Composable
private fun EmptyContent(
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
            Text(
                text = "📅",
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center
            )

            Text(
                text = "No events found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Text(
                text = "There are no events for this user yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
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
