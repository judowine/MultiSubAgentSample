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
import kotlinx.datetime.Instant
import org.example.project.judowine.domain.model.MeetingRecord

/**
 * Meeting Record Detail Screen for EventMeet application.
 *
 * Implemented by: compose-ui-architect (coordinated by project-orchestrator)
 * PBI-5, Task 9.2: MeetingRecordDetailScreen
 *
 * This screen displays the full details of a meeting record:
 * - Event title (fetched via eventId)
 * - User nickname
 * - Meeting date (formatted)
 * - Notes (full text, multiline)
 * - Tags (chips with FlowRow)
 *
 * Actions:
 * - Edit button → navigate to EditMeetingRecordScreen
 * - Delete button → show confirmation dialog → delete via ViewModel
 * - Navigate back after delete
 *
 * Design Notes:
 * - Follows Screen/Content separation pattern from PBI-1, PBI-2, PBI-3, PBI-4
 * - Integrates with MeetingRecordViewModel using MVI pattern
 * - Material3 design system
 * - NO direct data layer access (uses ViewModel only)
 *
 * @param meetingRecordId The database ID of the meeting record to display
 * @param viewModel The MeetingRecordViewModel managing state
 * @param onNavigateBack Callback to navigate back to list screen
 * @param onNavigateToEdit Callback to navigate to EditMeetingRecordScreen
 * @param modifier Optional modifier for customization
 */
@Composable
fun MeetingRecordDetailScreen(
    meetingRecordId: Long,
    viewModel: MeetingRecordViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // Observe detail UI state from ViewModel
    val detailUiState by viewModel.detailUiState.collectAsState()

    // Load meeting record detail on screen entry
    LaunchedEffect(meetingRecordId) {
        viewModel.handleIntent(MeetingRecordIntent.LoadMeetingRecordDetail(meetingRecordId))
    }

    // Handle deletion - navigate back when deleted
    LaunchedEffect(detailUiState) {
        if (detailUiState is MeetingRecordDetailUiState.Deleted) {
            onNavigateBack()
        }
    }

    MeetingRecordDetailContent(
        state = detailUiState,
        onNavigateBack = onNavigateBack,
        onEditClick = { recordId ->
            onNavigateToEdit(recordId)
        },
        onDeleteClick = { recordId ->
            viewModel.handleIntent(MeetingRecordIntent.DeleteMeetingRecord(recordId))
        },
        modifier = modifier
    )
}

/**
 * Stateless content composable for Meeting Record Detail.
 *
 * This component is designed to be reusable and testable. It receives all state
 * as parameters and emits events through callbacks (stateless pattern).
 *
 * @param state The current detail UI state (Idle, Loading, Success, Deleted, or Error)
 * @param onNavigateBack Callback when back button is clicked
 * @param onEditClick Callback when edit button is clicked
 * @param onDeleteClick Callback when delete button is clicked (after confirmation)
 * @param modifier Optional modifier for customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingRecordDetailContent(
    state: MeetingRecordDetailUiState,
    onNavigateBack: () -> Unit,
    onEditClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<MeetingRecord?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Meeting Details",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                    MeetingRecordDetailSuccessContent(
                        record = state.record,
                        onEditClick = { onEditClick(state.record.id) },
                        onDeleteClick = {
                            recordToDelete = state.record
                            showDeleteDialog = true
                        }
                    )
                }
                is MeetingRecordDetailUiState.Deleted -> {
                    // Navigation handled in LaunchedEffect
                    // Show nothing while navigating
                }
                is MeetingRecordDetailUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onBackClick = onNavigateBack
                    )
                }
            }

            // Delete confirmation dialog
            if (showDeleteDialog && recordToDelete != null) {
                DeleteConfirmationDialog(
                    nickname = recordToDelete!!.nickname,
                    onConfirm = {
                        onDeleteClick(recordToDelete!!.id)
                        showDeleteDialog = false
                        recordToDelete = null
                    },
                    onDismiss = {
                        showDeleteDialog = false
                        recordToDelete = null
                    }
                )
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
                text = "Loading meeting details...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Success state content - displays meeting record details with edit/delete actions.
 *
 * Layout structure:
 * - User nickname (headline)
 * - Event title (fetched via eventId, or placeholder)
 * - Meeting date (formatted)
 * - Notes section (full text, multiline)
 * - Tags section (chips with FlowRow)
 * - Action buttons (Edit, Delete)
 *
 * @param record The meeting record to display
 * @param onEditClick Callback when edit button is clicked
 * @param onDeleteClick Callback when delete button is clicked
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MeetingRecordDetailSuccessContent(
    record: MeetingRecord,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User nickname (headline)
        Text(
            text = record.nickname,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Divider()

        // Event title (placeholder - event lookup not in scope)
        InfoRow(
            label = "Event:",
            value = "Event ID: ${record.eventId}"
        )

        // Meeting date
        InfoRow(
            label = "Met on:",
            value = formatMeetingDate(record.createdAt)
        )

        Divider()

        // Notes section
        if (record.hasNotes()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Notes:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = record.notes ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Tags section
        if (record.hasTags()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tags:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    record.tags.forEach { tagName ->
                        AssistChip(
                            onClick = { /* Tags are not clickable in detail view */ },
                            label = {
                                Text(
                                    text = tagName,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Edit button
            OutlinedButton(
                onClick = onEditClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Edit",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Delete button
            Button(
                onClick = onDeleteClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(
                    text = "Delete",
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

/**
 * Delete confirmation dialog.
 *
 * Asks user to confirm before deleting a meeting record.
 *
 * @param nickname The nickname of the person to delete
 * @param onConfirm Callback when user confirms deletion
 * @param onDismiss Callback when user cancels or dismisses dialog
 */
@Composable
private fun DeleteConfirmationDialog(
    nickname: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Meeting Record?",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete the meeting record for \"$nickname\"? This action cannot be undone.",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "Delete",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}

/**
 * Reusable info row component for displaying label-value pairs.
 *
 * @param label The label text
 * @param value The value text
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.widthIn(min = 80.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Utility function to format meeting date for display.
 *
 * Shows relative time for recent meetings, absolute date for older ones.
 *
 * TODO: Replace with proper date formatting using kotlinx-datetime formatting APIs
 * when available in Compose Multiplatform.
 *
 * @param instant The meeting creation time
 * @return Formatted date string
 */
private fun formatMeetingDate(instant: Instant): String {
    // Basic formatting - replace with kotlinx-datetime formatting when available
    val dateStr = instant.toString().replace("T", " ").substringBefore(".")
    return dateStr
}
