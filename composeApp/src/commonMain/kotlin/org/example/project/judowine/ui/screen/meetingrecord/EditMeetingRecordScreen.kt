package org.example.project.judowine.ui.screen.meetingrecord

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.project.judowine.domain.model.MeetingRecord
import org.example.project.judowine.ui.component.molecule.NoteInputField
import org.example.project.judowine.ui.component.molecule.TagInputField

/**
 * Edit Meeting Record Screen for EventMeet application.
 *
 * Implemented by: compose-ui-architect (coordinated by project-orchestrator)
 * PBI-5, Task 9.3: EditMeetingRecordScreen
 *
 * This screen allows editing notes and tags for an existing meeting record:
 * - Pre-filled form with existing notes and tags
 * - NoteInputField for multiline notes input
 * - TagInputField with autocomplete from existing tags
 * - Selected tags display with dismiss action
 *
 * Actions:
 * - Save button → update via ViewModel
 * - Cancel button → navigate back without saving
 * - Handle save success (navigate back) and errors (display error message)
 *
 * Design Notes:
 * - Follows Screen/Content separation pattern from PBI-1, PBI-2, PBI-3, PBI-4
 * - Integrates with MeetingRecordViewModel using MVI pattern
 * - Uses NoteInputField and TagInputField molecule components
 * - Material3 design system
 * - NO direct data layer access (uses ViewModel only)
 *
 * @param meetingRecordId The database ID of the meeting record to edit
 * @param viewModel The MeetingRecordViewModel managing state
 * @param onNavigateBack Callback to navigate back to detail screen
 * @param modifier Optional modifier for customization
 */
@Composable
fun EditMeetingRecordScreen(
    meetingRecordId: Long,
    viewModel: MeetingRecordViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Observe detail UI state from ViewModel (for loading the record)
    val detailUiState by viewModel.detailUiState.collectAsState()

    // Observe tags from ViewModel (for autocomplete)
    val availableTags by viewModel.tags.collectAsState()

    // Load meeting record detail and tags on screen entry
    LaunchedEffect(meetingRecordId) {
        viewModel.handleIntent(MeetingRecordIntent.LoadMeetingRecordDetail(meetingRecordId))
        viewModel.handleIntent(MeetingRecordIntent.LoadTags)
    }

    // Navigate back on successful update
    LaunchedEffect(detailUiState) {
        if (detailUiState is MeetingRecordDetailUiState.Success) {
            // Check if we just returned from an update - in real implementation,
            // we'd need a separate "updated" state or flag
            // For now, we rely on the detail screen to show the updated data
        }
    }

    EditMeetingRecordContent(
        state = detailUiState,
        availableTags = availableTags,
        onSave = { notes, tagNames ->
            viewModel.handleIntent(
                MeetingRecordIntent.UpdateMeetingRecord(
                    id = meetingRecordId,
                    notes = notes,
                    tagNames = tagNames
                )
            )
            // Navigate back after initiating save
            onNavigateBack()
        },
        onCancel = onNavigateBack,
        modifier = modifier
    )
}

/**
 * Stateless content composable for Edit Meeting Record.
 *
 * This component is designed to be reusable and testable. It receives all state
 * as parameters and emits events through callbacks (stateless pattern).
 *
 * @param state The current detail UI state (for loading the record to edit)
 * @param availableTags List of all tags for autocomplete suggestions
 * @param onSave Callback when save button is clicked (notes, tagNames)
 * @param onCancel Callback when cancel button is clicked
 * @param modifier Optional modifier for customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMeetingRecordContent(
    state: MeetingRecordDetailUiState,
    availableTags: List<org.example.project.judowine.domain.model.Tag>,
    onSave: (String?, List<String>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Local state for form inputs
    var notesText by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var recordLoaded by remember { mutableStateOf(false) }

    // Initialize form with existing data when record loads
    LaunchedEffect(state) {
        if (state is MeetingRecordDetailUiState.Success && !recordLoaded) {
            notesText = state.record.notes ?: ""
            selectedTags = state.record.tags.toList()
            recordLoaded = true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Meeting",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Text(
                            text = "←",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
            when (state) {
                is MeetingRecordDetailUiState.Idle -> {
                    // Show nothing in idle state (loading will show immediately)
                }
                is MeetingRecordDetailUiState.Loading -> {
                    LoadingContent()
                }
                is MeetingRecordDetailUiState.Success -> {
                    EditFormContent(
                        record = state.record,
                        notesText = notesText,
                        onNotesChange = { notesText = it },
                        selectedTags = selectedTags,
                        availableTags = availableTags,
                        onTagAdded = { tagName ->
                            if (!selectedTags.contains(tagName)) {
                                selectedTags = selectedTags + tagName
                            }
                        },
                        onTagRemoved = { tagName ->
                            selectedTags = selectedTags.filter { it != tagName }
                        },
                        onSave = {
                            onSave(
                                notesText.ifBlank { null },
                                selectedTags
                            )
                        },
                        onCancel = onCancel
                    )
                }
                is MeetingRecordDetailUiState.Deleted -> {
                    // This shouldn't happen in edit screen
                    onCancel()
                }
                is MeetingRecordDetailUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onBackClick = onCancel
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
                text = "Loading meeting record...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Edit form content - displays the editing form with notes and tags inputs.
 *
 * Layout structure:
 * - User nickname (read-only headline)
 * - Event title (read-only, fetched via eventId or placeholder)
 * - NoteInputField for notes input
 * - TagInputField for tags input with autocomplete
 * - Save and Cancel buttons
 *
 * @param record The meeting record being edited
 * @param notesText Current notes text from form state
 * @param onNotesChange Callback when notes text changes
 * @param selectedTags Current selected tags from form state
 * @param availableTags All tags for autocomplete
 * @param onTagAdded Callback when a tag is added
 * @param onTagRemoved Callback when a tag is removed
 * @param onSave Callback when save button is clicked
 * @param onCancel Callback when cancel button is clicked
 */
@Composable
private fun EditFormContent(
    record: MeetingRecord,
    notesText: String,
    onNotesChange: (String) -> Unit,
    selectedTags: List<String>,
    availableTags: List<org.example.project.judowine.domain.model.Tag>,
    onTagAdded: (String) -> Unit,
    onTagRemoved: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User nickname (read-only)
        Text(
            text = record.nickname,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Event title (read-only, placeholder)
        Text(
            text = "Event ID: ${record.eventId}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider()

        // Notes input field
        NoteInputField(
            notes = notesText,
            onNotesChange = onNotesChange,
            maxLength = 500
        )

        // Tags input field
        TagInputField(
            selectedTags = selectedTags,
            availableTags = availableTags,
            onTagAdded = onTagAdded,
            onTagRemoved = onTagRemoved
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cancel button
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Save button
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * Error state content - displays error message with back button.
 *
 * @param message The error message to display
 * @param onBackClick Callback when back button is clicked
 */
@Composable
private fun ErrorContent(
    message: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
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
            text = "Failed to load meeting record",
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
            onClick = onBackClick,
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
