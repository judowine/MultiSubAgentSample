package org.example.project.judowine.ui.screen.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.judowine.domain.model.MeetingRecord
import org.example.project.judowine.domain.usecase.GetMeetingRecordsUseCase
import org.example.project.judowine.ui.model.PersonSummary

/**
 * ViewModel for the People List screen using MVI (Model-View-Intent) pattern.
 *
 * Implementation by: compose-ui-architect (coordinated by project-orchestrator)
 * PBI-6, Task 2: PeopleListViewModel with MVI pattern
 *
 * This ViewModel manages the state of the people list screen, aggregating meeting records
 * by person (userId) to create a summary view of all people the user has met.
 *
 * Architecture Notes:
 * - Follows Android UDF pattern: composeApp → shared (NO direct data layer access)
 * - Uses GetMeetingRecordsUseCase from domain layer (shared module)
 * - Aggregates domain models (MeetingRecord) into UI models (PersonSummary) in ViewModel
 * - Reactive: StateFlow automatically updates UI when meeting records change
 *
 * MVI Pattern:
 * - Model: PeopleListUiState sealed interface (type-safe state)
 * - View: PeopleListScreen observes uiState StateFlow
 * - Intent: PeopleListIntent sealed interface (user actions)
 *
 * State Transitions:
 * - Idle → LoadPeople → Loading → Success/Empty/Error
 * - * → Reset → Idle
 *
 * @property getMeetingRecordsUseCase Use case for retrieving all meeting records (injected from shared module)
 */
class PeopleListViewModel(
    private val getMeetingRecordsUseCase: GetMeetingRecordsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PeopleListUiState>(PeopleListUiState.Idle)
    val uiState: StateFlow<PeopleListUiState> = _uiState.asStateFlow()

    /**
     * Handle user intents (actions from UI).
     * This is the single entry point for all user interactions.
     *
     * @param intent The user action to handle
     */
    fun handleIntent(intent: PeopleListIntent) {
        when (intent) {
            is PeopleListIntent.LoadPeople -> loadPeople()
            is PeopleListIntent.Reset -> reset()
        }
    }

    /**
     * Load all people the user has met by aggregating meeting records.
     *
     * Business Logic:
     * 1. Fetch all meeting records from repository (via use case)
     * 2. Group meeting records by userId
     * 3. For each person, calculate:
     *    - Total meeting count
     *    - Most recent meeting date
     *    - Display nickname (from most recent record)
     * 4. Sort by last meeting date (most recent first)
     *
     * State Transitions:
     * - Idle → Loading → Success (if people exist)
     * - Idle → Loading → Empty (if no meeting records exist)
     * - Idle → Loading → Error (if repository fails)
     */
    private fun loadPeople() {
        _uiState.value = PeopleListUiState.Loading

        viewModelScope.launch {
            try {
                getMeetingRecordsUseCase.execute()
                    .collect { meetingRecords ->
                        if (meetingRecords.isEmpty()) {
                            _uiState.value = PeopleListUiState.Empty
                        } else {
                            val people = aggregatePeopleSummaries(meetingRecords)
                            _uiState.value = PeopleListUiState.Success(people)
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = PeopleListUiState.Error(
                    message = e.message ?: "Failed to load people"
                )
            }
        }
    }

    /**
     * Aggregate meeting records by person (userId) into PersonSummary list.
     *
     * Aggregation Logic:
     * - Group by userId
     * - Count total meetings per person
     * - Find most recent meeting date (max createdAt)
     * - Use nickname from any record (consistent for same userId)
     * - Sort by lastMeetingDate descending (most recent first)
     *
     * @param meetingRecords List of all meeting records from repository
     * @return List of PersonSummary, sorted by last meeting date (newest first)
     */
    private fun aggregatePeopleSummaries(meetingRecords: List<MeetingRecord>): List<PersonSummary> {
        return meetingRecords
            .groupBy { it.userId }
            .map { (userId, records) ->
                PersonSummary(
                    userId = userId,
                    nickname = records.first().nickname, // All records for same userId have same nickname
                    meetingCount = records.size,
                    lastMeetingDate = records.maxOf { it.createdAt }
                )
            }
            .sortedByDescending { it.lastMeetingDate } // Most recent meetings first
    }

    /**
     * Reset ViewModel state to Idle.
     * Useful for cleanup or re-initialization scenarios.
     */
    private fun reset() {
        _uiState.value = PeopleListUiState.Idle
    }
}

/**
 * Sealed interface representing all possible UI states for the People List screen.
 *
 * This follows the MVI pattern's "Model" component, ensuring type-safe state handling.
 * The UI can use exhaustive when-expressions to handle all possible states.
 *
 * States:
 * - Idle: Initial state before any action
 * - Loading: Fetching and aggregating meeting records
 * - Success: People list loaded successfully with data
 * - Empty: No meeting records exist (user hasn't met anyone yet)
 * - Error: Failed to load meeting records
 */
sealed interface PeopleListUiState {
    /**
     * Initial state before any data loading.
     */
    data object Idle : PeopleListUiState

    /**
     * Loading state while fetching meeting records from repository.
     */
    data object Loading : PeopleListUiState

    /**
     * Success state with aggregated people data.
     *
     * @property people List of PersonSummary, sorted by last meeting date (newest first)
     */
    data class Success(val people: List<PersonSummary>) : PeopleListUiState

    /**
     * Empty state when no meeting records exist.
     * This is distinct from Success with empty list - it indicates user hasn't met anyone yet.
     */
    data object Empty : PeopleListUiState

    /**
     * Error state when loading fails.
     *
     * @property message Error message to display to user
     */
    data class Error(val message: String) : PeopleListUiState
}

/**
 * Sealed interface representing all possible user intents (actions) for the People List screen.
 *
 * This follows the MVI pattern's "Intent" component, ensuring type-safe action handling.
 *
 * Intents:
 * - LoadPeople: User wants to load/refresh the people list
 * - Reset: User wants to reset the screen state
 */
sealed interface PeopleListIntent {
    /**
     * Load all people the user has met (aggregated from meeting records).
     */
    data object LoadPeople : PeopleListIntent

    /**
     * Reset ViewModel state to Idle.
     */
    data object Reset : PeopleListIntent
}
