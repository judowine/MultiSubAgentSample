package org.example.project.judowine.ui.screen.people

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.judowine.domain.model.MeetingRecord
import org.example.project.judowine.ui.component.atom.LoadingIndicator
import org.example.project.judowine.ui.component.molecule.ErrorDisplay
import org.example.project.judowine.ui.component.molecule.MeetingHistoryItem
import org.example.project.judowine.ui.component.molecule.TagFilter
import org.example.project.judowine.ui.model.PersonSummary

/**
 * Person Detail screen displaying all meetings with a specific person.
 *
 * Implementation by: compose-ui-architect (coordinated by project-orchestrator)
 * PBI-6, Task 5: PersonDetailScreen
 *
 * This screen follows the Screen/Content separation pattern:
 * - PersonDetailScreen: Stateful container (integrates with ViewModel)
 * - PersonDetailContent: Stateless presentation (receives data via parameters)
 *
 * Architecture:
 * - Uses MVI pattern via PersonDetailViewModel
 * - Observes uiState StateFlow for reactive updates
 * - Delegates rendering to stateless PersonDetailContent
 *
 * Features:
 * - Person summary header (nickname, total meetings)
 * - Tag filter (show all or filter by specific tag)
 * - Meeting history timeline (chronological, newest first)
 * - Navigation to EventDetailScreen and MeetingRecordDetailScreen
 *
 * @param userId connpass user ID of the person to display
 * @param viewModel ViewModel managing person detail state (injected)
 * @param onNavigateBack Callback for back navigation
 * @param onEventClick Callback when event is clicked (navigate to EventDetailScreen)
 * @param onMeetingClick Callback when meeting record is clicked (navigate to MeetingRecordDetailScreen)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    userId: Long,
    viewModel: PersonDetailViewModel,
    onNavigateBack: () -> Unit,
    onEventClick: (eventId: Long) -> Unit,
    onMeetingClick: (meetingRecordId: Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load person detail when screen launches
    LaunchedEffect(userId) {
        viewModel.handleIntent(PersonDetailIntent.LoadPersonDetail(userId))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (uiState) {
                        is PersonDetailUiState.Success -> {
                            Text((uiState as PersonDetailUiState.Success).person.nickname)
                        }
                        else -> {
                            Text("Person Detail")
                        }
                    }
                },
                navigationIcon = {
                    Button(onClick = onNavigateBack) {
                        Text("← Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        PersonDetailContent(
            uiState = uiState,
            onTagSelected = { tagName ->
                viewModel.handleIntent(PersonDetailIntent.FilterByTag(tagName))
            },
            onEventClick = onEventClick,
            onMeetingClick = onMeetingClick,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

/**
 * Stateless presentation component for Person Detail screen.
 *
 * This component renders different UI based on the current state:
 * - Idle: (no UI, waiting for LoadPersonDetail intent)
 * - Loading: Loading indicator
 * - Success: Person summary + tag filter + meeting history
 * - Error: Error display with message
 *
 * @param uiState Current UI state from ViewModel
 * @param onTagSelected Callback when a tag filter is selected
 * @param onEventClick Callback when event is clicked
 * @param onMeetingClick Callback when meeting record is clicked
 * @param modifier Modifier for customization (optional)
 */
@Composable
fun PersonDetailContent(
    uiState: PersonDetailUiState,
    onTagSelected: (String?) -> Unit,
    onEventClick: (eventId: Long) -> Unit,
    onMeetingClick: (meetingRecordId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is PersonDetailUiState.Idle -> {
            // No UI for Idle state (LoadPersonDetail intent will trigger immediately)
        }

        is PersonDetailUiState.Loading -> {
            LoadingContent()
        }

        is PersonDetailUiState.Success -> {
            SuccessContent(
                person = uiState.person,
                meetings = uiState.meetings,
                availableTags = uiState.availableTags,
                selectedTag = uiState.selectedTag,
                onTagSelected = onTagSelected,
                onEventClick = onEventClick,
                onMeetingClick = onMeetingClick,
                modifier = modifier
            )
        }

        is PersonDetailUiState.Error -> {
            ErrorContent(
                message = uiState.message,
                modifier = modifier
            )
        }
    }
}

/**
 * Loading state UI: Centered loading indicator.
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator()
    }
}

/**
 * Success state UI: Person summary + tag filter + meeting history.
 *
 * @param person Person summary (nickname, meeting count, last meeting date)
 * @param meetings List of meeting records (filtered by tag if selected)
 * @param availableTags All unique tags from this person's meetings
 * @param selectedTag Currently selected tag filter (null = showing all)
 * @param onTagSelected Callback when tag filter changes
 * @param onEventClick Callback when event is clicked
 * @param onMeetingClick Callback when meeting record is clicked
 * @param modifier Modifier for customization
 */
@Composable
private fun SuccessContent(
    person: PersonSummary,
    meetings: List<MeetingRecord>,
    availableTags: List<String>,
    selectedTag: String?,
    onTagSelected: (String?) -> Unit,
    onEventClick: (eventId: Long) -> Unit,
    onMeetingClick: (meetingRecordId: Long) -> Unit,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Person summary header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = person.nickname,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total meetings: ${person.meetingCount}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Tag filter (if tags exist)
        if (availableTags.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Filter by tag",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TagFilter(
                        availableTags = availableTags,
                        selectedTag = selectedTag,
                        onTagSelected = onTagSelected
                    )
                }
            }
        }

        // Meeting history section header
        item {
            Column {
                Text(
                    text = "Meeting History",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (selectedTag != null) {
                    Text(
                        text = "Showing meetings tagged with \"$selectedTag\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Meeting history items
        if (meetings.isNotEmpty()) {
            items(meetings, key = { it.id }) { meeting ->
                MeetingHistoryItem(
                    eventTitle = "Event #${meeting.eventId}", // Note: In real app, fetch event title from Event repository
                    eventDate = meeting.createdAt,
                    notesPreview = meeting.notePreview(maxLength = 100),
                    tags = meeting.tags,
                    onEventClick = { onEventClick(meeting.eventId) },
                    onMeetingClick = { onMeetingClick(meeting.id) }
                )
            }
        } else {
            // No meetings match the current tag filter
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No meetings found${if (selectedTag != null) " with tag \"$selectedTag\"" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Error state UI: Error message display.
 *
 * @param message Error message to display
 * @param modifier Modifier for customization
 */
@Composable
private fun ErrorContent(
    message: String,
    modifier: Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ErrorDisplay(message = message)
    }
}
