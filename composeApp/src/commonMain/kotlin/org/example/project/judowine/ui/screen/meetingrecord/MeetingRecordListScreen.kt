package org.example.project.judowine.ui.screen.meetingrecord

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.project.judowine.domain.model.MeetingRecord
import org.example.project.judowine.ui.component.molecule.MeetingRecordCard

/**
 * Meeting Record List Screen for EventMeet application.
 *
 * Implemented by: compose-ui-architect (coordinated by project-orchestrator)
 * PBI-4, Task 7.7: MeetingRecordListScreen
 *
 * This screen displays a list of all people the user has met at events:
 * - List of meeting records (grouped by person or chronologically)
 * - Shows: user nickname, event title, meeting date
 * - Empty state: "No meetings recorded yet"
 * - Loading state: CircularProgressIndicator
 * - Error state: Error message with retry button
 * - FAB: Navigate to AddMeetingRecordScreen
 *
 * Design Notes:
 * - Follows Screen/Content separation pattern from PBI-1, PBI-2, PBI-3
 * - Integrates with MeetingRecordViewModel using MVI pattern
 * - Stateless components for easy testing and reusability
 * - Material3 design system
 * - NO direct data layer access (uses ViewModel only)
 *
 * @param viewModel The MeetingRecordViewModel managing state
 * @param onNavigateToAdd Callback to navigate to AddMeetingRecordScreen
 * @param onMeetingRecordClick Callback when a meeting record is clicked (for PBI-5 detail view)
 * @param modifier Optional modifier for customization
 */
@Composable
fun MeetingRecordListScreen(
    viewModel: MeetingRecordViewModel,
    onNavigateToAdd: () -> Unit,
    onMeetingRecordClick: (MeetingRecord) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Observe UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Load meeting records on screen entry
    LaunchedEffect(Unit) {
        viewModel.handleIntent(MeetingRecordIntent.LoadMeetingRecords)
    }

    MeetingRecordListContent(
        state = uiState,
        onNavigateToAdd = onNavigateToAdd,
        onMeetingRecordClick = onMeetingRecordClick,
        onRetryClick = {
            viewModel.handleIntent(MeetingRecordIntent.LoadMeetingRecords)
        },
        modifier = modifier
    )
}

/**
 * Stateless content composable for Meeting Record List.
 *
 * This component is designed to be reusable and testable. It receives all state
 * as parameters and emits events through callbacks (stateless pattern).
 *
 * @param state The current UI state (Idle, Loading, Success, Empty, or Error)
 * @param onNavigateToAdd Callback when FAB is clicked
 * @param onMeetingRecordClick Callback when a meeting record is clicked
 * @param onRetryClick Callback when retry button is clicked (in error state)
 * @param modifier Optional modifier for customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingRecordListContent(
    state: MeetingRecordUiState,
    onNavigateToAdd: () -> Unit,
    onMeetingRecordClick: (MeetingRecord) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "People Met",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    text = "➕",
                    style = MaterialTheme.typography.headlineSmall
                )
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
                is MeetingRecordUiState.Idle -> {
                    // Show nothing in idle state (loading will show immediately)
                }
                is MeetingRecordUiState.Loading -> {
                    LoadingContent()
                }
                is MeetingRecordUiState.Success -> {
                    MeetingRecordListSuccessContent(
                        records = state.records,
                        onMeetingRecordClick = onMeetingRecordClick
                    )
                }
                is MeetingRecordUiState.Empty -> {
                    EmptyContent()
                }
                is MeetingRecordUiState.Error -> {
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
                text = "Loading meeting records...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Success state content - displays a scrollable list of meeting records.
 *
 * Layout structure:
 * - Header showing count of meeting records
 * - Scrollable list of MeetingRecordCard components
 *
 * Note: Event title lookup is deferred to PBI-5 or later.
 * For now, shows "Event ID: {eventId}" as placeholder.
 *
 * @param records List of meeting records to display
 * @param onMeetingRecordClick Callback when a record is clicked
 */
@Composable
private fun MeetingRecordListSuccessContent(
    records: List<MeetingRecord>,
    onMeetingRecordClick: (MeetingRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with count
        item {
            Text(
                text = "${records.size} person${if (records.size != 1) "s" else ""} met",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Meeting records list
        items(
            items = records,
            key = { record -> record.id }
        ) { record ->
            MeetingRecordCard(
                meetingRecord = record,
                eventTitle = null, // TODO: Look up event title from eventId (PBI-5 or later)
                onClick = { onMeetingRecordClick(record) }
            )
        }
    }
}

/**
 * Empty state content - displays when no meeting records exist.
 *
 * Shows:
 * - Large icon
 * - Title: "No meetings recorded yet"
 * - Description: Encouragement to start recording meetings
 */
@Composable
private fun EmptyContent(
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
            text = "👥",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No meetings recorded yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start recording people you meet at events by tapping the + button",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
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
            text = "Failed to load meeting records",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

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
