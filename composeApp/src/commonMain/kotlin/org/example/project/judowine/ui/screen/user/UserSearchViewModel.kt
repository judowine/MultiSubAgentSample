package org.example.project.judowine.ui.screen.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.example.project.judowine.domain.model.ConnpassUser
import org.example.project.judowine.domain.usecase.SearchUsersUseCase

/**
 * ViewModel for User Search & Discovery using MVI (Model-View-Intent) pattern.
 *
 * Implemented by: compose-ui-architect
 * PBI-3, Task 5.5: UserSearchViewModel for user search feature
 *
 * This ViewModel manages state for the UserSearchScreen:
 * - Real-time search with debouncing to avoid excessive API calls
 * - Displays search results from connpass API
 * - Handles loading, success, empty, and error states
 * - No local caching (API-only search per PBI-3 requirements)
 *
 * MVI Pattern Components:
 * - **Model**: UserSearchUiState (sealed interface representing all possible states)
 * - **View**: UserSearchScreen observing stateFlow and emitting intents
 * - **Intent**: UserSearchIntent (sealed interface representing user actions)
 *
 * State Flow:
 * ```
 * User Action (Intent) → ViewModel.handleIntent() → Update State → UI observes StateFlow → Re-render
 * ```
 *
 * Design Notes:
 * - Follows MVI pattern established in PBI-1 (ProfileViewModel) and PBI-2 (EventViewModel)
 * - Uses Kotlin StateFlow for reactive state management
 * - Implements search debouncing (300ms) to reduce API calls
 * - Follows Android UDF: ViewModel → Use Cases (domain layer)
 * - NO direct data layer access (strict layer isolation)
 * - API-only search (no local caching in PBI-3 scope)
 *
 * @property searchUsersUseCase Use case for searching connpass users
 */
class UserSearchViewModel(
    private val searchUsersUseCase: SearchUsersUseCase
) : ViewModel() {

    // State Management
    private val _uiState = MutableStateFlow<UserSearchUiState>(UserSearchUiState.Idle)
    val uiState: StateFlow<UserSearchUiState> = _uiState.asStateFlow()

    // Search query flow for debouncing
    private val _searchQuery = MutableStateFlow("")

    init {
        // Setup debounced search listener
        setupDebouncedSearch()
    }

    /**
     * Setup debounced search to avoid excessive API calls.
     *
     * Debouncing strategy:
     * - Wait 300ms after user stops typing before triggering search
     * - Ignore empty queries (return to Idle state)
     * - Only trigger search when query actually changes
     */
    @OptIn(FlowPreview::class)
    private fun setupDebouncedSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // Wait 300ms after user stops typing
                .distinctUntilChanged() // Only trigger when query actually changes
                .collect { query ->
                    if (query.isBlank()) {
                        _uiState.value = UserSearchUiState.Idle
                    } else {
                        performSearch(query)
                    }
                }
        }
    }

    /**
     * Handle user intents (actions) and update state accordingly.
     *
     * This is the single entry point for all user interactions.
     *
     * @param intent The user intent to process
     */
    fun handleIntent(intent: UserSearchIntent) {
        when (intent) {
            is UserSearchIntent.Search -> updateSearchQuery(intent.query)
            is UserSearchIntent.ClearResults -> clearResults()
            is UserSearchIntent.ClearError -> clearError()
        }
    }

    /**
     * Update search query and trigger debounced search.
     *
     * @param query The search query entered by user
     */
    private fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Perform actual search via use case.
     *
     * State transitions:
     * Idle/Success/Empty/Error → Loading → Success/Empty/Error
     *
     * @param query The search query (already validated and trimmed by use case)
     */
    private fun performSearch(query: String) {
        _uiState.value = UserSearchUiState.Loading

        viewModelScope.launch {
            try {
                val result = searchUsersUseCase.execute(query)

                result.fold(
                    onSuccess = { users ->
                        _uiState.value = if (users.isEmpty()) {
                            UserSearchUiState.Empty
                        } else {
                            UserSearchUiState.Success(users)
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value = UserSearchUiState.Error(
                            message = exception.message ?: "Failed to search users"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UserSearchUiState.Error(
                    message = e.message ?: "Unexpected error while searching users"
                )
            }
        }
    }

    /**
     * Clear search results and return to idle state.
     */
    private fun clearResults() {
        _searchQuery.value = ""
        _uiState.value = UserSearchUiState.Idle
    }

    /**
     * Clear error state and return to idle.
     */
    private fun clearError() {
        _uiState.value = UserSearchUiState.Idle
    }
}

/**
 * MVI State - Represents all possible UI states for user search.
 *
 * This sealed interface ensures type-safe state handling with exhaustive when-expressions.
 *
 * State Transitions:
 * ```
 * Idle → Loading → Success | Empty | Error
 * Success/Empty → Loading → Success | Empty | Error (via new search)
 * Error → Idle (via ClearError intent)
 * Any → Idle (via ClearResults intent)
 * ```
 */
sealed interface UserSearchUiState {
    /**
     * Idle state - initial state or no search performed yet.
     */
    data object Idle : UserSearchUiState

    /**
     * Loading state - searching for users via API.
     */
    data object Loading : UserSearchUiState

    /**
     * Success state - users found and loaded successfully.
     *
     * @property users List of ConnpassUser domain models (API-only, not cached)
     */
    data class Success(val users: List<ConnpassUser>) : UserSearchUiState

    /**
     * Empty state - no users found for the search query.
     */
    data object Empty : UserSearchUiState

    /**
     * Error state - failed to search users (API error).
     *
     * @property message The error message to display
     */
    data class Error(val message: String) : UserSearchUiState
}

/**
 * MVI Intent - Represents all possible user actions.
 *
 * Intents are processed by ViewModel.handleIntent() and result in state changes.
 */
sealed interface UserSearchIntent {
    /**
     * Search for users by nickname.
     *
     * This intent triggers debounced search (300ms delay after user stops typing).
     *
     * @property query User's nickname to search for (partial match supported)
     */
    data class Search(val query: String) : UserSearchIntent

    /**
     * Clear search results and return to idle state.
     */
    data object ClearResults : UserSearchIntent

    /**
     * Clear error state.
     */
    data object ClearError : UserSearchIntent
}
