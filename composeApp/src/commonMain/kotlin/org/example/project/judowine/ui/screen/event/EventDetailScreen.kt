package org.example.project.judowine.ui.screen.event

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.project.judowine.domain.model.Event
import org.example.project.judowine.ui.component.organism.EventHeaderSection
import org.example.project.judowine.ui.component.organism.EventParticipantsSection
import org.example.project.judowine.ui.component.organism.EventDescriptionSection

/**
 * Event Detail Screen for EventMeet application.
 *
 * Implemented by: compose-ui-architect
 * PBI-2, Task 4.7: EventDetailScreen
 * Enhanced by: compose-ui-architect (PBI-4, Task 7.9: Add Person Met FAB)
 *
 * This screen displays detailed information for a specific event:
 * - Full title and description
 * - Start and end datetime
 * - Location/address
 * - Organizer info
 * - Participant counts (accepted/waiting/limit)
 * - Event URL (clickable)
 * - FAB: "Add Person Met" (navigates to AddMeetingRecordScreen with pre-selected event)
 *
 * Design Notes:
 * - Follows Screen/Content separation pattern from PBI-1
 * - Integrates with EventViewModel using MVI pattern
 * - Handles Loading, Success, and Error states
 * - Stateless components for easy testing and reusability
 * - NO direct data layer access (uses ViewModel only)
 * - Uses Material3 design system for consistency
 *
 * @param viewModel The EventViewModel managing state
 * @param eventId The connpass event ID to display
 * @param onNavigateBack Callback to navigate back to previous screen
 * @param onAddPersonMet Callback to navigate to AddMeetingRecordScreen (PBI-4)
 * @param modifier Optional modifier for customization
 */
@Composable
fun EventDetailScreen(
    viewModel: EventViewModel,
    eventId: Long,
    onNavigateBack: () -> Unit = {},
    onAddPersonMet: (Event) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Observe UI state from ViewModel
    val uiState by viewModel.eventDetailState.collectAsState()

    // Load event detail on screen entry
    LaunchedEffect(eventId) {
        viewModel.handleIntent(EventIntent.LoadEventDetail(eventId))
    }

    EventDetailContent(
        state = uiState,
        onNavigateBack = onNavigateBack,
        onAddPersonMet = onAddPersonMet,
        onRetryClick = {
            viewModel.handleIntent(EventIntent.LoadEventDetail(eventId))
        },
        modifier = modifier
    )
}

/**
 * Stateless content composable for Event Detail.
 *
 * This component is designed to be reusable and testable. It receives all state
 * as parameters and emits events through callbacks (stateless pattern).
 *
 * @param state The current detail state (Idle, Loading, Success, or Error)
 * @param onNavigateBack Callback when back button is clicked
 * @param onAddPersonMet Callback when "Add Person Met" FAB is clicked (PBI-4)
 * @param onRetryClick Callback when retry button is clicked (in error state)
 * @param modifier Optional modifier for customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailContent(
    state: EventDetailUiState,
    onNavigateBack: () -> Unit,
    onAddPersonMet: (Event) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            )
        },
        floatingActionButton = {
            // Show FAB only when event is successfully loaded
            if (state is EventDetailUiState.Success) {
                FloatingActionButton(
                    onClick = { onAddPersonMet(state.event) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "➕",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Add Person Met",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
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
                is EventDetailUiState.Idle -> {
                    // Show nothing in idle state
                }
                is EventDetailUiState.Loading -> {
                    LoadingContent()
                }
                is EventDetailUiState.Success -> {
                    EventDetailSuccessContent(event = state.event)
                }
                is EventDetailUiState.Error -> {
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
 * Success state content - displays full event details.
 *
 * Layout structure:
 * - Header section (title, date/time, location)
 * - Participants section (accepted, waiting, limit)
 * - Description section (full event description)
 * - Action section (event URL button)
 *
 * @param event The event to display
 */
@Composable
private fun EventDetailSuccessContent(
    event: Event,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .safeContentPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section
        EventHeaderSection(event = event)

        HorizontalDivider()

        // Participants Section
        EventParticipantsSection(event = event)

        HorizontalDivider()

        // Description Section
        EventDescriptionSection(event = event)

        Spacer(modifier = Modifier.height(8.dp))

        // Action Section - Event URL Button
        EventActionSection(event = event)
    }
}

/**
 * Event action section - displays event URL button.
 *
 * @param event The event to display
 */
@Composable
private fun EventActionSection(
    event: Event,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Event URL Button
        Button(
            onClick = {
                // TODO: Open URL in browser
                // This requires platform-specific implementation via expect/actual
                println("Opening URL: ${event.url}")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "View on Connpass",
                style = MaterialTheme.typography.labelLarge
            )
        }

        // URL text for reference
        Text(
            text = event.url,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
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
