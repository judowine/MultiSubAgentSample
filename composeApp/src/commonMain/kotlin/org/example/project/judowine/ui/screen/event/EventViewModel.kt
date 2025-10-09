package org.example.project.judowine.ui.screen.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.judowine.domain.model.Event
import org.example.project.judowine.domain.model.MeetingRecord
import org.example.project.judowine.domain.usecase.GetEventDetailUseCase
import org.example.project.judowine.domain.usecase.GetEventsUseCase
import org.example.project.judowine.domain.usecase.GetMeetingRecordsByEventUseCase

/**
 * ViewModel for Event Discovery using MVI (Model-View-Intent) pattern.
 *
 * Implemented by: compose-ui-architect
 * PBI-2, Task 4.8: EventViewModel with MVI pattern
 * Enhanced by: compose-ui-architect
 * PBI-7, Task 1: Added meeting records for event support
 *
 * This ViewModel manages state for both EventList and EventDetail screens:
 * - EventListScreen: Browse events with pull-to-refresh
 * - EventDetailScreen: View detailed event information + people met at event
 *
 * MVI Pattern Components:
 * - **Model**: EventListUiState, EventDetailUiState (sealed interfaces representing all possible states)
 * - **View**: Composable screens observing stateFlow and emitting intents
 * - **Intent**: EventIntent (sealed interface representing user actions)
 *
 * State Flow:
 * ```
 * User Action (Intent) → ViewModel.handleIntent() → Update State → UI observes StateFlow → Re-render
 * ```
 *
 * Design Notes:
 * - Follows MVI pattern established in PBI-1 (ProfileViewModel.kt)
 * - Uses Kotlin StateFlow for reactive state management
 * - Separate state management for list, detail, and meeting records views
 * - Follows Android UDF: ViewModel → Use Cases (domain layer)
 * - NO direct data layer access (strict layer isolation)
 * - Implements caching strategy for offline support
 *
 * @property getEventsUseCase Use case for retrieving event lists
 * @property getEventDetailUseCase Use case for retrieving individual event details
 * @property getMeetingRecordsByEventUseCase Use case for retrieving meeting records for a specific event (PBI-7)
 */
class EventViewModel(
    private val getEventsUseCase: GetEventsUseCase,
    private val getEventDetailUseCase: GetEventDetailUseCase,
    private val getMeetingRecordsByEventUseCase: GetMeetingRecordsByEventUseCase
) : ViewModel() {

    // State Management - Event List
    private val _eventListState = MutableStateFlow<EventListUiState>(EventListUiState.Idle)
    val eventListState: StateFlow<EventListUiState> = _eventListState.asStateFlow()

    // State Management - Event Detail
    private val _eventDetailState = MutableStateFlow<EventDetailUiState>(EventDetailUiState.Idle)
    val eventDetailState: StateFlow<EventDetailUiState> = _eventDetailState.asStateFlow()

    // State Management - Meeting Records for Event (PBI-7)
    private val _meetingRecordsForEventState = MutableStateFlow<List<MeetingRecord>>(emptyList())
    val meetingRecordsForEventState: StateFlow<List<MeetingRecord>> = _meetingRecordsForEventState.asStateFlow()

    /**
     * Handle user intents (actions) and update state accordingly.
     *
     * This is the single entry point for all user interactions.
     *
     * @param intent The user intent to process
     */
    fun handleIntent(intent: EventIntent) {
        when (intent) {
            is EventIntent.LoadEvents -> loadEvents(intent.nickname, intent.forceRefresh)
            is EventIntent.LoadEventDetail -> loadEventDetail(intent.eventId)
            is EventIntent.RefreshEvents -> refreshEvents(intent.nickname)
            is EventIntent.LoadMeetingRecordsForEvent -> loadMeetingRecordsForEvent(intent.eventId)
            is EventIntent.ClearError -> clearError()
            is EventIntent.Reset -> reset()
        }
    }

    /**
     * Load events for a specific user.
     *
     * Strategy:
     * - forceRefresh = false: Network-first with cache fallback (offline support)
     * - forceRefresh = true: Always fetch from API, clear cache first
     *
     * State transitions:
     * Idle/Error → Loading → Success/Empty/Error
     *
     * @param nickname connpass user nickname to fetch events for
     * @param forceRefresh If true, forces fresh API fetch and cache clear
     */
    private fun loadEvents(nickname: String, forceRefresh: Boolean) {
        _eventListState.value = EventListUiState.Loading

        viewModelScope.launch {
            try {
                // Check cache status for better UX
                val isCacheEmpty = getEventsUseCase.isCacheEmpty()

                // If cache is available and not forcing refresh, show cached data immediately
                if (!forceRefresh && !isCacheEmpty) {
                    val cachedEvents = getEventsUseCase.getCachedEvents()
                    if (cachedEvents.isNotEmpty()) {
                        _eventListState.value = EventListUiState.Success(cachedEvents.sortedByDescending { it.startedAt })
                    }
                }

                // Fetch from repository (network-first with cache fallback)
                val result = getEventsUseCase.execute(nickname, forceRefresh)

                result.fold(
                    onSuccess = { events ->
                        _eventListState.value = if (events.isEmpty()) {
                            EventListUiState.Empty
                        } else {
                            // Sort by date (newest first)
                            EventListUiState.Success(events.sortedByDescending { it.startedAt })
                        }
                    },
                    onFailure = { exception ->
                        // If we already showed cached data, keep it and just log the error
                        if (_eventListState.value is EventListUiState.Success) {
                            // Keep current cached data, don't show error
                            return@fold
                        }

                        _eventListState.value = EventListUiState.Error(
                            message = exception.message ?: "Failed to load events"
                        )
                    }
                )
            } catch (e: Exception) {
                _eventListState.value = EventListUiState.Error(
                    message = e.message ?: "Unexpected error while loading events"
                )
            }
        }
    }

    /**
     * Refresh events (pull-to-refresh scenario).
     *
     * Forces fresh API fetch and cache clear. This is a convenience method
     * that delegates to loadEvents with forceRefresh = true.
     *
     * @param nickname connpass user nickname to fetch events for
     */
    private fun refreshEvents(nickname: String) {
        loadEvents(nickname, forceRefresh = true)
    }

    /**
     * Load detailed information for a specific event.
     *
     * Strategy:
     * - Cache-first with API fallback for optimal performance
     * - Returns null if event not found
     *
     * State transitions:
     * Idle/Error → Loading → Success/Error
     *
     * @param eventId connpass event ID to fetch
     */
    private fun loadEventDetail(eventId: Long) {
        _eventDetailState.value = EventDetailUiState.Loading

        viewModelScope.launch {
            try {
                // Check cache first for instant display
                val cachedEvent = getEventDetailUseCase.getCachedEventById(eventId)
                if (cachedEvent != null) {
                    _eventDetailState.value = EventDetailUiState.Success(cachedEvent)
                }

                // Fetch from repository (cache-first with API fallback)
                val result = getEventDetailUseCase.execute(eventId)

                result.fold(
                    onSuccess = { event ->
                        _eventDetailState.value = if (event != null) {
                            EventDetailUiState.Success(event)
                        } else {
                            EventDetailUiState.Error(
                                message = "Event not found"
                            )
                        }
                    },
                    onFailure = { exception ->
                        // If we already showed cached data, keep it
                        if (_eventDetailState.value is EventDetailUiState.Success) {
                            return@fold
                        }

                        _eventDetailState.value = EventDetailUiState.Error(
                            message = exception.message ?: "Failed to load event details"
                        )
                    }
                )
            } catch (e: Exception) {
                _eventDetailState.value = EventDetailUiState.Error(
                    message = e.message ?: "Unexpected error while loading event details"
                )
            }
        }
    }

    /**
     * Clear error state and return to idle.
     */
    private fun clearError() {
        if (_eventListState.value is EventListUiState.Error) {
            _eventListState.value = EventListUiState.Idle
        }
        if (_eventDetailState.value is EventDetailUiState.Error) {
            _eventDetailState.value = EventDetailUiState.Idle
        }
    }

    /**
     * Load meeting records for a specific event.
     *
     * This function loads all meeting records associated with the given event ID
     * and updates the meetingRecordsForEventState StateFlow reactively.
     *
     * Strategy:
     * - Uses GetMeetingRecordsByEventUseCase which returns a Flow<List<MeetingRecord>>
     * - Collects the Flow continuously for reactive updates when records are added/deleted
     * - Initializes with empty list, then updates as records are emitted
     *
     * State transitions:
     * Empty list → List of meeting records (reactive)
     *
     * @param eventId connpass event ID to fetch meeting records for
     */
    private fun loadMeetingRecordsForEvent(eventId: Long) {
        viewModelScope.launch {
            try {
                // Execute use case and collect Flow for reactive updates
                getMeetingRecordsByEventUseCase.execute(eventId)
                    .collect { meetingRecords ->
                        _meetingRecordsForEventState.value = meetingRecords
                    }
            } catch (e: Exception) {
                // Log error but don't crash - reset to empty list
                println("Error loading meeting records for event $eventId: ${e.message}")
                _meetingRecordsForEventState.value = emptyList()
            }
        }
    }

    /**
     * Reset all states to idle.
     */
    private fun reset() {
        _eventListState.value = EventListUiState.Idle
        _eventDetailState.value = EventDetailUiState.Idle
        _meetingRecordsForEventState.value = emptyList()
    }
}

/**
 * MVI State - Represents all possible UI states for event list.
 *
 * This sealed interface ensures type-safe state handling with exhaustive when-expressions.
 *
 * State Transitions:
 * ```
 * Idle → Loading → Success | Empty | Error
 * Error → Idle (via ClearError intent)
 * Success → Loading → Success | Empty | Error (via RefreshEvents intent)
 * ```
 */
sealed interface EventListUiState {
    /**
     * Idle state - initial state or after reset.
     */
    data object Idle : EventListUiState

    /**
     * Loading state - fetching events from repository.
     */
    data object Loading : EventListUiState

    /**
     * Success state - events loaded successfully.
     *
     * @property events List of events (sorted by date, newest first)
     */
    data class Success(val events: List<Event>) : EventListUiState

    /**
     * Empty state - no events found for user.
     */
    data object Empty : EventListUiState

    /**
     * Error state - failed to load events.
     *
     * @property message The error message to display
     */
    data class Error(val message: String) : EventListUiState
}

/**
 * MVI State - Represents all possible UI states for event detail.
 *
 * State Transitions:
 * ```
 * Idle → Loading → Success | Error
 * Error → Idle (via ClearError intent)
 * ```
 */
sealed interface EventDetailUiState {
    /**
     * Idle state - initial state or after reset.
     */
    data object Idle : EventDetailUiState

    /**
     * Loading state - fetching event details.
     */
    data object Loading : EventDetailUiState

    /**
     * Success state - event details loaded successfully.
     *
     * @property event The event details
     */
    data class Success(val event: Event) : EventDetailUiState

    /**
     * Error state - failed to load event details.
     *
     * @property message The error message to display
     */
    data class Error(val message: String) : EventDetailUiState
}

/**
 * MVI Intent - Represents all possible user actions.
 *
 * Intents are processed by ViewModel.handleIntent() and result in state changes.
 */
sealed interface EventIntent {
    /**
     * Load events for a user.
     *
     * @property nickname connpass user nickname to fetch events for
     * @property forceRefresh If true, forces fresh API fetch and cache clear
     */
    data class LoadEvents(
        val nickname: String,
        val forceRefresh: Boolean = false
    ) : EventIntent

    /**
     * Load detailed information for a specific event.
     *
     * @property eventId connpass event ID to fetch
     */
    data class LoadEventDetail(val eventId: Long) : EventIntent

    /**
     * Refresh events (pull-to-refresh scenario).
     *
     * @property nickname connpass user nickname to fetch events for
     */
    data class RefreshEvents(val nickname: String) : EventIntent

    /**
     * Load meeting records for a specific event.
     *
     * PBI-7: Event-Centric Meeting Review
     *
     * @property eventId connpass event ID to fetch meeting records for
     */
    data class LoadMeetingRecordsForEvent(val eventId: Long) : EventIntent

    /**
     * Clear error state.
     */
    data object ClearError : EventIntent

    /**
     * Reset all states to idle.
     */
    data object Reset : EventIntent
}
