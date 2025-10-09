package org.example.project.judowine.ui.screen.meetingrecord

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.project.judowine.domain.model.ConnpassUser
import org.example.project.judowine.domain.model.Event
import org.example.project.judowine.ui.screen.user.UserSearchScreen
import org.example.project.judowine.ui.screen.user.UserSearchViewModel

/**
 * Add Meeting Record Screen for EventMeet application.
 *
 * Implemented by: compose-ui-architect (coordinated by project-orchestrator)
 * PBI-4, Task 7.8: AddMeetingRecordScreen
 *
 * This screen enables users to create a new meeting record with a two-step flow:
 * 1. Event Selection (pre-selected if coming from EventDetailScreen, else manual selection)
 * 2. User Selection (integrates UserSearchScreen for user discovery)
 * 3. Confirmation step before saving
 *
 * Features:
 * - Pre-filled event when navigating from EventDetailScreen
 * - User search integration (reuses UserSearchScreen)
 * - Confirmation display (event title + user nickname)
 * - Save button triggers SaveMeetingRecordUseCase
 * - Duplicate detection error handling
 * - Navigation back on success
 *
 * Design Notes:
 * - Follows Screen/Content separation pattern from PBI-1, PBI-2, PBI-3
 * - Integrates with MeetingRecordViewModel for saving
 * - Reuses UserSearchViewModel for user selection
 * - Stateless components for easy testing
 * - Material3 design system
 * - NO direct data layer access (uses ViewModels only)
 *
 * @param meetingRecordViewModel The MeetingRecordViewModel for saving records
 * @param userSearchViewModel The UserSearchViewModel for user selection
 * @param preSelectedEvent Optional pre-selected event (when navigating from EventDetailScreen)
 * @param onNavigateBack Callback to navigate back on success or cancel
 * @param modifier Optional modifier for customization
 */
@Composable
fun AddMeetingRecordScreen(
    meetingRecordViewModel: MeetingRecordViewModel,
    userSearchViewModel: UserSearchViewModel,
    preSelectedEvent: Event? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Track selected event and user
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var selectedUser by remember { mutableStateOf<ConnpassUser?>(null) }

    // Update selected event when preSelectedEvent changes
    LaunchedEffect(preSelectedEvent) {
        if (preSelectedEvent != null) {
            selectedEvent = preSelectedEvent
        }
    }

    // Track current step
    var currentStep by remember {
        mutableStateOf(AddMeetingStep.EVENT_SELECTION)
    }

    // Update current step when selectedEvent is set
    LaunchedEffect(selectedEvent) {
        if (selectedEvent != null && currentStep == AddMeetingStep.EVENT_SELECTION) {
            currentStep = AddMeetingStep.USER_SELECTION
        }
    }

    // Observe ViewModel state for error handling
    val uiState by meetingRecordViewModel.uiState.collectAsState()

    // Handle save success/error
    LaunchedEffect(uiState) {
        when (uiState) {
            is MeetingRecordUiState.Success -> {
                // Successfully saved, navigate back
                onNavigateBack()
            }
            else -> { /* Handle other states if needed */ }
        }
    }

    AddMeetingRecordContent(
        currentStep = currentStep,
        selectedEvent = selectedEvent,
        selectedUser = selectedUser,
        uiState = uiState,
        userSearchViewModel = userSearchViewModel,
        onEventSelected = { event ->
            selectedEvent = event
            currentStep = AddMeetingStep.USER_SELECTION
        },
        onUserSelected = { user ->
            selectedUser = user
            currentStep = AddMeetingStep.CONFIRMATION
        },
        onConfirmSave = {
            selectedEvent?.let { event ->
                selectedUser?.let { user ->
                    meetingRecordViewModel.handleIntent(
                        MeetingRecordIntent.CreateMeetingRecord(
                            eventId = event.eventId,
                            userId = user.userId.toLong(),
                            nickname = user.nickname
                        )
                    )
                }
            }
        },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

/**
 * Enum representing the steps in the add meeting record flow.
 */
enum class AddMeetingStep {
    EVENT_SELECTION,
    USER_SELECTION,
    CONFIRMATION
}

/**
 * Stateless content composable for Add Meeting Record.
 *
 * @param currentStep The current step in the flow
 * @param selectedEvent The currently selected event (if any)
 * @param selectedUser The currently selected user (if any)
 * @param uiState The current ViewModel state (for error handling)
 * @param userSearchViewModel ViewModel for user search step
 * @param onEventSelected Callback when an event is selected
 * @param onUserSelected Callback when a user is selected
 * @param onConfirmSave Callback when save is confirmed
 * @param onNavigateBack Callback to navigate back
 * @param modifier Optional modifier for customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeetingRecordContent(
    currentStep: AddMeetingStep,
    selectedEvent: Event?,
    selectedUser: ConnpassUser?,
    uiState: MeetingRecordUiState,
    userSearchViewModel: UserSearchViewModel,
    onEventSelected: (Event) -> Unit,
    onUserSelected: (ConnpassUser) -> Unit,
    onConfirmSave: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentStep) {
                            AddMeetingStep.EVENT_SELECTION -> "Select Event"
                            AddMeetingStep.USER_SELECTION -> "Select Person"
                            AddMeetingStep.CONFIRMATION -> "Confirm Meeting"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = MaterialTheme.typography.headlineMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentStep) {
                AddMeetingStep.EVENT_SELECTION -> {
                    // Note: Event selection UI deferred to later PBI or simplified
                    // For PBI-4, we assume event is pre-selected from EventDetailScreen
                    EventSelectionPlaceholder(
                        onNavigateBack = onNavigateBack
                    )
                }
                AddMeetingStep.USER_SELECTION -> {
                    // Reuse UserSearchScreen for user selection
                    UserSearchScreen(
                        viewModel = userSearchViewModel,
                        onUserClick = onUserSelected
                    )
                }
                AddMeetingStep.CONFIRMATION -> {
                    ConfirmationStep(
                        event = selectedEvent,
                        user = selectedUser,
                        uiState = uiState,
                        onConfirmSave = onConfirmSave,
                        onNavigateBack = onNavigateBack
                    )
                }
            }
        }
    }
}

/**
 * Placeholder for event selection step.
 *
 * Note: Event selection UI is simplified for PBI-4.
 * Primary use case is navigation from EventDetailScreen with pre-selected event.
 * Full event selection UI can be added in future PBIs.
 */
@Composable
private fun EventSelectionPlaceholder(
    onNavigateBack: () -> Unit,
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
            text = "📅",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Event Selection",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please navigate from an event detail screen to add a person met at that event.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp)
        ) {
            Text(
                text = "Go Back",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Confirmation step - displays selected event and user before saving.
 *
 * Shows:
 * - Selected event title
 * - Selected user nickname
 * - Save button (triggers SaveMeetingRecordUseCase)
 * - Error message if duplicate or save fails
 *
 * @param event The selected event
 * @param user The selected user
 * @param uiState Current ViewModel state (for error handling)
 * @param onConfirmSave Callback when save button is clicked
 * @param onNavigateBack Callback to navigate back
 */
@Composable
private fun ConfirmationStep(
    event: Event?,
    user: ConnpassUser?,
    uiState: MeetingRecordUiState,
    onConfirmSave: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .safeContentPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "✅",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Confirm Meeting Record",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Selected Event Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Event",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = event?.title ?: "Unknown Event",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Selected User Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Person Met",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = user?.nickname ?: "Unknown User",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Error message (if any)
        if (uiState is MeetingRecordUiState.Error) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Save Button
        Button(
            onClick = onConfirmSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = event != null && user != null
        ) {
            Text(
                text = "Save Meeting Record",
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Cancel Button
        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Cancel",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
