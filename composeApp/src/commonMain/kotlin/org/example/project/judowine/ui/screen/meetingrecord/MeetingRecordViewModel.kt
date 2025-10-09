package org.example.project.judowine.ui.screen.meetingrecord

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.example.project.judowine.domain.model.MeetingRecord
import org.example.project.judowine.domain.model.Tag
import org.example.project.judowine.domain.usecase.GetMeetingRecordsUseCase
import org.example.project.judowine.domain.usecase.SaveMeetingRecordUseCase
import org.example.project.judowine.domain.usecase.UpdateMeetingRecordUseCase
import org.example.project.judowine.domain.usecase.DeleteMeetingRecordUseCase
import org.example.project.judowine.domain.usecase.GetAllTagsUseCase

/**
 * ViewModel for Meeting Records using MVI (Model-View-Intent) pattern.
 *
 * Implemented by: compose-ui-architect (coordinated by project-orchestrator)
 * PBI-4, Task 7.6: MeetingRecordViewModel with MVI pattern
 * PBI-5, Task 9.1: Extended with notes/tags management and detail screen support
 *
 * This ViewModel manages state for meeting record screens:
 * - MeetingRecordListScreen: Browse all meeting records
 * - AddMeetingRecordScreen: Create new meeting records
 * - MeetingRecordDetailScreen: View full meeting record details (PBI-5)
 * - EditMeetingRecordScreen: Edit notes and tags for meeting records (PBI-5)
 *
 * MVI Pattern Components:
 * - **Model**: MeetingRecordUiState (sealed interface representing all possible states)
 * - **View**: Composable screens observing stateFlow and emitting intents
 * - **Intent**: MeetingRecordIntent (sealed interface representing user actions)
 *
 * State Flow:
 * ```
 * User Action (Intent) → ViewModel.handleIntent() → Update State → UI observes StateFlow → Re-render
 * ```
 *
 * Design Notes:
 * - Follows MVI pattern established in PBI-1, PBI-2, PBI-3
 * - Uses Kotlin StateFlow for reactive state management
 * - Follows Android UDF: ViewModel → Use Cases (domain layer)
 * - NO direct data layer access (strict layer isolation)
 * - Handles duplicate detection via SaveMeetingRecordUseCase
 * - Manages tag autocomplete suggestions for UI
 *
 * @property saveMeetingRecordUseCase Use case for saving meeting records
 * @property getMeetingRecordsUseCase Use case for retrieving meeting records
 * @property updateMeetingRecordUseCase Use case for updating meeting records (PBI-5)
 * @property deleteMeetingRecordUseCase Use case for deleting meeting records (PBI-5)
 * @property getAllTagsUseCase Use case for retrieving all tags (PBI-5)
 */
class MeetingRecordViewModel(
    private val saveMeetingRecordUseCase: SaveMeetingRecordUseCase,
    private val getMeetingRecordsUseCase: GetMeetingRecordsUseCase,
    private val updateMeetingRecordUseCase: UpdateMeetingRecordUseCase,
    private val deleteMeetingRecordUseCase: DeleteMeetingRecordUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase
) : ViewModel() {

    // State Management - Meeting Records List
    private val _uiState = MutableStateFlow<MeetingRecordUiState>(MeetingRecordUiState.Idle)
    val uiState: StateFlow<MeetingRecordUiState> = _uiState.asStateFlow()

    // State Management - Meeting Record Detail (PBI-5)
    private val _detailUiState = MutableStateFlow<MeetingRecordDetailUiState>(MeetingRecordDetailUiState.Idle)
    val detailUiState: StateFlow<MeetingRecordDetailUiState> = _detailUiState.asStateFlow()

    // State Management - Tags for Autocomplete (PBI-5)
    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags.asStateFlow()

    /**
     * Handle user intents (actions) and update state accordingly.
     *
     * This is the single entry point for all user interactions.
     *
     * @param intent The user intent to process
     */
    fun handleIntent(intent: MeetingRecordIntent) {
        when (intent) {
            is MeetingRecordIntent.LoadMeetingRecords -> loadMeetingRecords()
            is MeetingRecordIntent.CreateMeetingRecord -> createMeetingRecord(
                eventId = intent.eventId,
                userId = intent.userId,
                nickname = intent.nickname
            )
            is MeetingRecordIntent.UpdateMeetingRecord -> updateMeetingRecord(
                id = intent.id,
                notes = intent.notes,
                tagNames = intent.tagNames
            )
            is MeetingRecordIntent.DeleteMeetingRecord -> deleteMeetingRecord(intent.id)
            is MeetingRecordIntent.LoadTags -> loadTags()
            is MeetingRecordIntent.LoadMeetingRecordDetail -> loadMeetingRecordDetail(intent.id)
        }
    }

    /**
     * Load all meeting records.
     *
     * This method collects from the Flow returned by GetMeetingRecordsUseCase,
     * enabling reactive updates whenever meeting records change in the database.
     *
     * State transitions:
     * Idle/Error → Loading → Success/Empty/Error
     */
    private fun loadMeetingRecords() {
        _uiState.value = MeetingRecordUiState.Loading

        viewModelScope.launch {
            try {
                getMeetingRecordsUseCase.execute()
                    .catch { exception ->
                        _uiState.value = MeetingRecordUiState.Error(
                            message = exception.message ?: "Failed to load meeting records"
                        )
                    }
                    .collect { records ->
                        _uiState.value = if (records.isEmpty()) {
                            MeetingRecordUiState.Empty
                        } else {
                            MeetingRecordUiState.Success(records)
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = MeetingRecordUiState.Error(
                    message = e.message ?: "Unexpected error while loading meeting records"
                )
            }
        }
    }

    /**
     * Create a new meeting record.
     *
     * Business rules:
     * - eventId must be positive (valid event ID)
     * - userId must be positive (valid user ID)
     * - nickname must not be blank
     * - Duplicate detection handled by SaveMeetingRecordUseCase
     *
     * On success: State returns to Success with updated records (via Flow)
     * On failure: State transitions to Error with descriptive message
     *
     * @param eventId connpass event ID where the meeting occurred
     * @param userId connpass user ID of the person met
     * @param nickname User's display name
     */
    private fun createMeetingRecord(
        eventId: Long,
        userId: Long,
        nickname: String
    ) {
        // Keep current state visible while creating (don't show loading)
        viewModelScope.launch {
            try {
                val result = saveMeetingRecordUseCase.execute(
                    eventId = eventId,
                    userId = userId,
                    nickname = nickname
                )

                result.fold(
                    onSuccess = {
                        // Success - the Flow will automatically update the UI
                        // No need to manually update state here
                    },
                    onFailure = { exception ->
                        _uiState.value = MeetingRecordUiState.Error(
                            message = exception.message ?: "Failed to save meeting record"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = MeetingRecordUiState.Error(
                    message = e.message ?: "Unexpected error while saving meeting record"
                )
            }
        }
    }

    /**
     * Update an existing meeting record with notes and tags.
     *
     * PBI-5, Task 9.1: Update meeting record implementation
     *
     * Business rules:
     * - id must be positive (valid database ID)
     * - notes can be null or empty (optional)
     * - tagNames can be empty list (remove all tags)
     *
     * On success: Returns to detail state with updated record (via Flow)
     * On failure: State transitions to Error with descriptive message
     *
     * @param id The meeting record database ID
     * @param notes Optional text memo about the meeting
     * @param tagNames List of tag names to associate
     */
    private fun updateMeetingRecord(
        id: Long,
        notes: String?,
        tagNames: List<String>
    ) {
        viewModelScope.launch {
            try {
                val result = updateMeetingRecordUseCase(
                    id = id,
                    notes = notes,
                    tagNames = tagNames
                )

                result.fold(
                    onSuccess = {
                        // Success - reload the detail to show updated data
                        loadMeetingRecordDetail(id)
                    },
                    onFailure = { exception ->
                        _detailUiState.value = MeetingRecordDetailUiState.Error(
                            message = exception.message ?: "Failed to update meeting record"
                        )
                    }
                )
            } catch (e: Exception) {
                _detailUiState.value = MeetingRecordDetailUiState.Error(
                    message = e.message ?: "Unexpected error while updating meeting record"
                )
            }
        }
    }

    /**
     * Delete a meeting record.
     *
     * PBI-5, Task 9.1: Delete meeting record implementation
     *
     * Business rules:
     * - id must be positive (valid database ID)
     * - Foreign key CASCADE will automatically delete associated tag cross-refs
     *
     * On success: State transitions to indicate deletion (for navigation back)
     * On failure: State transitions to Error with descriptive message
     *
     * @param id The meeting record ID to delete
     */
    private fun deleteMeetingRecord(id: Long) {
        viewModelScope.launch {
            try {
                val result = deleteMeetingRecordUseCase(id)

                result.fold(
                    onSuccess = {
                        // Success - indicate deletion for UI to navigate back
                        _detailUiState.value = MeetingRecordDetailUiState.Deleted
                    },
                    onFailure = { exception ->
                        _detailUiState.value = MeetingRecordDetailUiState.Error(
                            message = exception.message ?: "Failed to delete meeting record"
                        )
                    }
                )
            } catch (e: Exception) {
                _detailUiState.value = MeetingRecordDetailUiState.Error(
                    message = e.message ?: "Unexpected error while deleting meeting record"
                )
            }
        }
    }

    /**
     * Load all tags for autocomplete suggestions.
     *
     * PBI-5, Task 9.1: Load tags for autocomplete
     *
     * This method collects from the Flow returned by GetAllTagsUseCase,
     * enabling reactive updates whenever tags change in the database.
     */
    private fun loadTags() {
        viewModelScope.launch {
            try {
                getAllTagsUseCase()
                    .catch { exception ->
                        // Log error but don't fail - tags are optional
                        println("Failed to load tags: ${exception.message}")
                        _tags.value = emptyList()
                    }
                    .collect { tags ->
                        _tags.value = tags
                    }
            } catch (e: Exception) {
                // Log error but don't fail - tags are optional
                println("Unexpected error while loading tags: ${e.message}")
                _tags.value = emptyList()
            }
        }
    }

    /**
     * Load a single meeting record detail by ID.
     *
     * PBI-5, Task 9.1: Load meeting record detail
     *
     * This method finds the specific meeting record from the list state
     * to display in the detail screen.
     *
     * State transitions:
     * Idle/Error → Loading → Success/Error
     *
     * @param id The meeting record database ID
     */
    private fun loadMeetingRecordDetail(id: Long) {
        _detailUiState.value = MeetingRecordDetailUiState.Loading

        viewModelScope.launch {
            try {
                getMeetingRecordsUseCase.execute()
                    .catch { exception ->
                        _detailUiState.value = MeetingRecordDetailUiState.Error(
                            message = exception.message ?: "Failed to load meeting record"
                        )
                    }
                    .collect { records ->
                        val record = records.find { it.id == id }
                        _detailUiState.value = if (record != null) {
                            MeetingRecordDetailUiState.Success(record)
                        } else {
                            MeetingRecordDetailUiState.Error(
                                message = "Meeting record not found"
                            )
                        }
                    }
            } catch (e: Exception) {
                _detailUiState.value = MeetingRecordDetailUiState.Error(
                    message = e.message ?: "Unexpected error while loading meeting record"
                )
            }
        }
    }
}

/**
 * MVI State - Represents all possible UI states for meeting records.
 *
 * This sealed interface ensures type-safe state handling with exhaustive when-expressions.
 *
 * State Transitions:
 * ```
 * Idle → Loading → Success | Empty | Error
 * Success → Loading → Success | Empty | Error (via refresh)
 * Error → Loading → Success | Empty | Error (via retry)
 * ```
 */
sealed interface MeetingRecordUiState {
    /**
     * Idle state - initial state before loading.
     */
    data object Idle : MeetingRecordUiState

    /**
     * Loading state - fetching meeting records from repository.
     */
    data object Loading : MeetingRecordUiState

    /**
     * Success state - meeting records loaded successfully.
     *
     * @property records List of meeting records (ordered by createdAt DESC - newest first)
     */
    data class Success(val records: List<MeetingRecord>) : MeetingRecordUiState

    /**
     * Empty state - no meeting records found.
     */
    data object Empty : MeetingRecordUiState

    /**
     * Error state - failed to load or save meeting records.
     *
     * @property message The error message to display
     */
    data class Error(val message: String) : MeetingRecordUiState
}

/**
 * MVI State - Represents all possible UI states for meeting record detail screen.
 *
 * PBI-5, Task 9.1: Detail screen state management
 *
 * This sealed interface ensures type-safe state handling for the detail screen.
 *
 * State Transitions:
 * ```
 * Idle → Loading → Success | Error
 * Success → Loading → Success | Error (via refresh)
 * Success → Deleted (via delete action)
 * ```
 */
sealed interface MeetingRecordDetailUiState {
    /**
     * Idle state - initial state before loading.
     */
    data object Idle : MeetingRecordDetailUiState

    /**
     * Loading state - fetching meeting record details.
     */
    data object Loading : MeetingRecordDetailUiState

    /**
     * Success state - meeting record loaded successfully.
     *
     * @property record The meeting record with notes and tags
     */
    data class Success(val record: MeetingRecord) : MeetingRecordDetailUiState

    /**
     * Deleted state - meeting record was deleted successfully.
     * UI should navigate back to list screen.
     */
    data object Deleted : MeetingRecordDetailUiState

    /**
     * Error state - failed to load, update, or delete meeting record.
     *
     * @property message The error message to display
     */
    data class Error(val message: String) : MeetingRecordDetailUiState
}

/**
 * MVI Intent - Represents all possible user actions.
 *
 * Intents are processed by ViewModel.handleIntent() and result in state changes.
 */
sealed interface MeetingRecordIntent {
    /**
     * Load all meeting records.
     */
    data object LoadMeetingRecords : MeetingRecordIntent

    /**
     * Create a new meeting record.
     *
     * @property eventId connpass event ID where the meeting occurred
     * @property userId connpass user ID of the person met
     * @property nickname User's display name
     */
    data class CreateMeetingRecord(
        val eventId: Long,
        val userId: Long,
        val nickname: String
    ) : MeetingRecordIntent

    /**
     * Update an existing meeting record with notes and tags.
     *
     * PBI-5, Task 9.1: Update meeting record intent
     *
     * @property id The meeting record database ID
     * @property notes Optional text memo about the meeting
     * @property tagNames List of tag names to associate
     */
    data class UpdateMeetingRecord(
        val id: Long,
        val notes: String?,
        val tagNames: List<String>
    ) : MeetingRecordIntent

    /**
     * Delete a meeting record.
     *
     * PBI-5, Task 9.1: Delete meeting record intent
     *
     * @property id The meeting record ID to delete
     */
    data class DeleteMeetingRecord(val id: Long) : MeetingRecordIntent

    /**
     * Load all tags for autocomplete suggestions.
     *
     * PBI-5, Task 9.1: Load tags intent
     */
    data object LoadTags : MeetingRecordIntent

    /**
     * Load a single meeting record detail by ID.
     *
     * PBI-5, Task 9.1: Load meeting record detail intent
     *
     * @property id The meeting record database ID
     */
    data class LoadMeetingRecordDetail(val id: Long) : MeetingRecordIntent
}
