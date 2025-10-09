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
 * ViewModel for the Person Detail screen using MVI (Model-View-Intent) pattern.
 *
 * Implementation by: compose-ui-architect (coordinated by project-orchestrator)
 * PBI-6, Task 4: PersonDetailViewModel with MVI pattern
 *
 * This ViewModel manages the state of the person detail screen, displaying all meetings
 * with a specific person and supporting tag-based filtering of those meetings.
 *
 * Architecture Notes:
 * - Follows Android UDF pattern: composeApp → shared (NO direct data layer access)
 * - Uses GetMeetingRecordsUseCase from domain layer (shared module)
 * - Filters domain models (MeetingRecord) by userId in ViewModel
 * - Supports client-side tag filtering without additional repository queries
 * - Reactive: StateFlow automatically updates UI when meeting records change
 *
 * MVI Pattern:
 * - Model: PersonDetailUiState sealed interface (type-safe state)
 * - View: PersonDetailScreen observes uiState StateFlow
 * - Intent: PersonDetailIntent sealed interface (user actions)
 *
 * State Transitions:
 * - Idle → LoadPersonDetail(userId) → Loading → Success/Error
 * - Success → FilterByTag(tag) → Success (with filtered meetings)
 * - * → Reset → Idle
 *
 * @property getMeetingRecordsUseCase Use case for retrieving all meeting records (injected from shared module)
 */
class PersonDetailViewModel(
    private val getMeetingRecordsUseCase: GetMeetingRecordsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PersonDetailUiState>(PersonDetailUiState.Idle)
    val uiState: StateFlow<PersonDetailUiState> = _uiState.asStateFlow()

    private var allMeetings: List<MeetingRecord> = emptyList()
    private var currentUserId: Long? = null
    private var currentSelectedTag: String? = null

    /**
     * Handle user intents (actions from UI).
     * This is the single entry point for all user interactions.
     *
     * @param intent The user action to handle
     */
    fun handleIntent(intent: PersonDetailIntent) {
        when (intent) {
            is PersonDetailIntent.LoadPersonDetail -> loadPersonDetail(intent.userId)
            is PersonDetailIntent.FilterByTag -> filterByTag(intent.tagName)
            is PersonDetailIntent.Reset -> reset()
        }
    }

    /**
     * Load person detail and all meetings with this person.
     *
     * Business Logic:
     * 1. Fetch all meeting records from repository (via use case)
     * 2. Filter meetings by userId
     * 3. Calculate person summary (meeting count, last meeting date)
     * 4. Extract unique tags from all meetings
     * 5. Sort meetings chronologically (newest first)
     *
     * State Transitions:
     * - Idle → Loading → Success (if meetings exist for this userId)
     * - Idle → Loading → Error (if repository fails)
     */
    private fun loadPersonDetail(userId: Long) {
        _uiState.value = PersonDetailUiState.Loading
        currentUserId = userId
        currentSelectedTag = null

        viewModelScope.launch {
            try {
                getMeetingRecordsUseCase.execute()
                    .collect { allRecords ->
                        allMeetings = allRecords
                        applyFilter()
                    }
            } catch (e: Exception) {
                _uiState.value = PersonDetailUiState.Error(
                    message = e.message ?: "Failed to load person detail"
                )
            }
        }
    }

    /**
     * Filter meetings by tag name.
     * If tagName is null, shows all meetings for this person (clears filter).
     *
     * This is client-side filtering - no repository query needed.
     *
     * @param tagName Tag name to filter by (null = show all)
     */
    private fun filterByTag(tagName: String?) {
        currentSelectedTag = tagName
        applyFilter()
    }

    /**
     * Apply current filter (userId + optional tag) to all meetings and update state.
     *
     * This method is called:
     * - After loading all meetings from repository
     * - After user changes tag filter
     */
    private fun applyFilter() {
        val userId = currentUserId ?: return

        // Filter by userId
        val personMeetings = allMeetings.filter { it.userId == userId }

        if (personMeetings.isEmpty()) {
            _uiState.value = PersonDetailUiState.Error(
                message = "No meetings found for this person"
            )
            return
        }

        // Apply tag filter (if selected)
        val filteredMeetings = if (currentSelectedTag != null) {
            personMeetings.filter { meeting ->
                meeting.tags.contains(currentSelectedTag)
            }
        } else {
            personMeetings
        }

        // Sort by creation date (newest first)
        val sortedMeetings = filteredMeetings.sortedByDescending { it.createdAt }

        // Calculate person summary
        val person = PersonSummary(
            userId = userId,
            nickname = personMeetings.first().nickname,
            meetingCount = personMeetings.size,
            lastMeetingDate = personMeetings.maxOf { it.createdAt }
        )

        // Extract all unique tags from person's meetings (for filter UI)
        val allTags = personMeetings
            .flatMap { it.tags }
            .distinct()
            .sorted()

        _uiState.value = PersonDetailUiState.Success(
            person = person,
            meetings = sortedMeetings,
            availableTags = allTags,
            selectedTag = currentSelectedTag
        )
    }

    /**
     * Reset ViewModel state to Idle.
     * Useful for cleanup or re-initialization scenarios.
     */
    private fun reset() {
        _uiState.value = PersonDetailUiState.Idle
        allMeetings = emptyList()
        currentUserId = null
        currentSelectedTag = null
    }
}

/**
 * Sealed interface representing all possible UI states for the Person Detail screen.
 *
 * This follows the MVI pattern's "Model" component, ensuring type-safe state handling.
 * The UI can use exhaustive when-expressions to handle all possible states.
 *
 * States:
 * - Idle: Initial state before any action
 * - Loading: Fetching meeting records from repository
 * - Success: Person detail loaded successfully with meetings and filter state
 * - Error: Failed to load person detail
 */
sealed interface PersonDetailUiState {
    /**
     * Initial state before any data loading.
     */
    data object Idle : PersonDetailUiState

    /**
     * Loading state while fetching meeting records from repository.
     */
    data object Loading : PersonDetailUiState

    /**
     * Success state with person detail and meeting history.
     *
     * @property person Person summary (nickname, meeting count, last meeting date)
     * @property meetings List of MeetingRecord for this person, filtered by selected tag (if any), sorted chronologically (newest first)
     * @property availableTags All unique tags from this person's meetings (for filter UI)
     * @property selectedTag Currently selected tag filter (null = showing all meetings)
     */
    data class Success(
        val person: PersonSummary,
        val meetings: List<MeetingRecord>,
        val availableTags: List<String>,
        val selectedTag: String?
    ) : PersonDetailUiState

    /**
     * Error state when loading fails or no meetings exist for this person.
     *
     * @property message Error message to display to user
     */
    data class Error(val message: String) : PersonDetailUiState
}

/**
 * Sealed interface representing all possible user intents (actions) for the Person Detail screen.
 *
 * This follows the MVI pattern's "Intent" component, ensuring type-safe action handling.
 *
 * Intents:
 * - LoadPersonDetail: User wants to load a specific person's detail
 * - FilterByTag: User wants to filter meetings by tag
 * - Reset: User wants to reset the screen state
 */
sealed interface PersonDetailIntent {
    /**
     * Load person detail for a specific userId.
     *
     * @property userId connpass user ID
     */
    data class LoadPersonDetail(val userId: Long) : PersonDetailIntent

    /**
     * Filter person's meetings by tag name.
     *
     * @property tagName Tag name to filter by (null = show all meetings)
     */
    data class FilterByTag(val tagName: String?) : PersonDetailIntent

    /**
     * Reset ViewModel state to Idle.
     */
    data object Reset : PersonDetailIntent
}
