package org.example.project.judowine.ui.screen.meetingrecord

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.example.project.judowine.domain.model.MeetingRecord
import org.example.project.judowine.domain.usecase.GetMeetingRecordsUseCase
import org.example.project.judowine.domain.usecase.SaveMeetingRecordUseCase

/**
 * ViewModel for Meeting Records using MVI (Model-View-Intent) pattern.
 *
 * Implemented by: compose-ui-architect (coordinated by project-orchestrator)
 * PBI-4, Task 7.6: MeetingRecordViewModel with MVI pattern
 *
 * This ViewModel manages state for meeting record screens:
 * - MeetingRecordListScreen: Browse all meeting records
 * - AddMeetingRecordScreen: Create new meeting records
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
 *
 * @property saveMeetingRecordUseCase Use case for saving meeting records
 * @property getMeetingRecordsUseCase Use case for retrieving meeting records
 */
class MeetingRecordViewModel(
    private val saveMeetingRecordUseCase: SaveMeetingRecordUseCase,
    private val getMeetingRecordsUseCase: GetMeetingRecordsUseCase
) : ViewModel() {

    // State Management - Meeting Records
    private val _uiState = MutableStateFlow<MeetingRecordUiState>(MeetingRecordUiState.Idle)
    val uiState: StateFlow<MeetingRecordUiState> = _uiState.asStateFlow()

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
            is MeetingRecordIntent.DeleteMeetingRecord -> deleteMeetingRecord(intent.id)
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
     * Delete a meeting record.
     *
     * Note: This functionality is not in PBI-4 scope (deferred to PBI-5).
     * Placeholder implementation for future use.
     *
     * @param id The meeting record ID to delete
     */
    private fun deleteMeetingRecord(id: Long) {
        // TODO: Implement in PBI-5 when delete functionality is added
        // For now, this is a placeholder to complete the Intent interface
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
     * Delete a meeting record.
     *
     * Note: Not in PBI-4 scope, deferred to PBI-5.
     *
     * @property id The meeting record ID to delete
     */
    data class DeleteMeetingRecord(val id: Long) : MeetingRecordIntent
}
