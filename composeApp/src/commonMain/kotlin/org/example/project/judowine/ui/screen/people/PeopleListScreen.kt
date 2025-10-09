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
import org.example.project.judowine.ui.component.atom.LoadingIndicator
import org.example.project.judowine.ui.component.molecule.ErrorDisplay
import org.example.project.judowine.ui.component.molecule.PersonCard
import org.example.project.judowine.ui.model.PersonSummary

/**
 * People List screen displaying all people the user has met.
 *
 * Implementation by: compose-ui-architect (coordinated by project-orchestrator)
 * PBI-6, Task 3: PeopleListScreen
 *
 * This screen follows the Screen/Content separation pattern:
 * - PeopleListScreen: Stateful container (integrates with ViewModel)
 * - PeopleListContent: Stateless presentation (receives data via parameters)
 *
 * Architecture:
 * - Uses MVI pattern via PeopleListViewModel
 * - Observes uiState StateFlow for reactive updates
 * - Delegates rendering to stateless PeopleListContent
 *
 * User Actions:
 * - Load people on screen launch
 * - Tap person card to navigate to PersonDetailScreen
 *
 * @param viewModel ViewModel managing people list state (injected)
 * @param onPersonClick Callback when a person is clicked (navigate to PersonDetailScreen)
 */
@Composable
fun PeopleListScreen(
    viewModel: PeopleListViewModel,
    onPersonClick: (userId: Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load people when screen launches
    LaunchedEffect(Unit) {
        viewModel.handleIntent(PeopleListIntent.LoadPeople)
    }

    PeopleListContent(
        uiState = uiState,
        onPersonClick = onPersonClick
    )
}

/**
 * Stateless presentation component for People List screen.
 *
 * This component renders different UI based on the current state:
 * - Idle: (no UI, waiting for LoadPeople intent)
 * - Loading: Loading indicator
 * - Success: LazyColumn of PersonCard items
 * - Empty: Empty state message
 * - Error: Error display with message
 *
 * @param uiState Current UI state from ViewModel
 * @param onPersonClick Callback when a person is clicked
 * @param modifier Modifier for customization (optional)
 */
@Composable
fun PeopleListContent(
    uiState: PeopleListUiState,
    onPersonClick: (userId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is PeopleListUiState.Idle -> {
            // No UI for Idle state (LoadPeople intent will trigger immediately)
        }

        is PeopleListUiState.Loading -> {
            LoadingContent()
        }

        is PeopleListUiState.Success -> {
            SuccessContent(
                people = uiState.people,
                onPersonClick = onPersonClick,
                modifier = modifier
            )
        }

        is PeopleListUiState.Empty -> {
            EmptyContent()
        }

        is PeopleListUiState.Error -> {
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
 * Success state UI: List of people met.
 *
 * @param people List of PersonSummary (sorted by last meeting date, newest first)
 * @param onPersonClick Callback when a person is clicked
 * @param modifier Modifier for customization
 */
@Composable
private fun SuccessContent(
    people: List<PersonSummary>,
    onPersonClick: (userId: Long) -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "People You've Met",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${people.size} ${if (people.size == 1) "person" else "people"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // People list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(people, key = { it.userId }) { person ->
                PersonCard(
                    nickname = person.nickname,
                    meetingCount = person.meetingCount,
                    lastMeetingDate = person.lastMeetingDate,
                    onClick = { onPersonClick(person.userId) }
                )
            }
        }
    }
}

/**
 * Empty state UI: Message when no meeting records exist.
 */
@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No people met yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Create meeting records to see your connections",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
