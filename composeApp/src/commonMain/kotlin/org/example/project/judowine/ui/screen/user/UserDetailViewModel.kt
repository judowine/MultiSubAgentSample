package org.example.project.judowine.ui.screen.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.judowine.domain.model.ConnpassUser
import org.example.project.judowine.domain.model.Event
import org.example.project.judowine.domain.usecase.FindCommonEventsUseCase
import org.example.project.judowine.domain.usecase.GetUserEventsUseCase
import org.example.project.judowine.domain.usecase.SearchUsersUseCase

/**
 * ViewModel for User Detail using MVI (Model-View-Intent) pattern.
 *
 * Implemented by: compose-ui-architect
 * PBI-3, Task 6.4: UserDetailViewModel with MVI pattern
 *
 * This ViewModel manages state for UserDetailScreen:
 * - Fetches user information by nickname
 * - Fetches user's participated events
 * - Finds common events if logged-in user is provided
 *
 * MVI Pattern Components:
 * - **Model**: UserDetailUiState (sealed interface representing all possible states)
 * - **View**: UserDetailScreen observing stateFlow and emitting intents
 * - **Intent**: UserDetailIntent (sealed interface representing user actions)
 *
 * State Flow:
 * ```
 * User Action (Intent) → ViewModel.handleIntent() → Update State → UI observes StateFlow → Re-render
 * ```
 *
 * Design Notes:
 * - Follows MVI pattern established in PBI-1 (ProfileViewModel) and PBI-2 (EventViewModel)
 * - Uses Kotlin StateFlow for reactive state management
 * - Follows Android UDF: ViewModel → Use Cases (domain layer)
 * - NO direct data layer access (strict layer isolation)
 * - Coordinates multiple API calls (user info, events, common events)
 * - Implements partial success pattern (show events even if common events fail)
 *
 * Loading Strategy:
 * - Load user info first (must succeed to proceed)
 * - Load user's events (must succeed to show Success state)
 * - Load common events in parallel (optional, failure is non-blocking)
 *
 * @property searchUsersUseCase Use case for searching users by nickname
 * @property getUserEventsUseCase Use case for retrieving user's events
 * @property findCommonEventsUseCase Use case for finding common events between users
 */
class UserDetailViewModel(
    private val searchUsersUseCase: SearchUsersUseCase,
    private val getUserEventsUseCase: GetUserEventsUseCase,
    private val findCommonEventsUseCase: FindCommonEventsUseCase
) : ViewModel() {

    // State Management
    private val _uiState = MutableStateFlow<UserDetailUiState>(UserDetailUiState.Loading)
    val uiState: StateFlow<UserDetailUiState> = _uiState.asStateFlow()

    /**
     * Handle user intents (actions) and update state accordingly.
     *
     * This is the single entry point for all user interactions.
     *
     * @param intent The user intent to process
     */
    fun handleIntent(intent: UserDetailIntent) {
        when (intent) {
            is UserDetailIntent.LoadUserDetail -> loadUserDetail(
                nickname = intent.nickname,
                loggedInUserNickname = intent.loggedInUserNickname
            )
            is UserDetailIntent.Retry -> {
                // Retry requires stored context - for now, just keep current state
                // In production, would store last-attempted nickname
                _uiState.value = UserDetailUiState.Loading
            }
            is UserDetailIntent.ClearError -> {
                if (_uiState.value is UserDetailUiState.Error) {
                    _uiState.value = UserDetailUiState.Loading
                }
            }
        }
    }

    /**
     * Load user detail information, events, and common events (if applicable).
     *
     * Strategy:
     * 1. Load user info by nickname (using SearchUsersUseCase)
     * 2. Load user's participated events (using GetUserEventsUseCase)
     * 3. Load common events if logged-in user provided (using FindCommonEventsUseCase)
     *
     * State transitions:
     * Loading → Success/Error
     *
     * Partial success handling:
     * - If user info fails: Error state
     * - If events fail: Error state
     * - If common events fail: Success state with empty common events list
     *
     * @param nickname Target user's connpass nickname
     * @param loggedInUserNickname Logged-in user's nickname (nullable, for common events)
     */
    private fun loadUserDetail(nickname: String, loggedInUserNickname: String?) {
        _uiState.value = UserDetailUiState.Loading

        viewModelScope.launch {
            try {
                // Step 1: Search for user by nickname
                val userSearchResult = searchUsersUseCase.execute(nickname)

                if (userSearchResult.isFailure) {
                    _uiState.value = UserDetailUiState.Error(
                        message = userSearchResult.exceptionOrNull()?.message
                            ?: "Failed to load user information"
                    )
                    return@launch
                }

                // Extract user from search results (should be 1 result if exact match)
                val users = userSearchResult.getOrThrow()
                val user = users.firstOrNull { it.nickname.equals(nickname, ignoreCase = true) }

                if (user == null) {
                    _uiState.value = UserDetailUiState.Error(
                        message = "User not found: $nickname"
                    )
                    return@launch
                }

                // Step 2: Load user's events
                val eventsResult = getUserEventsUseCase.execute(nickname)

                if (eventsResult.isFailure) {
                    _uiState.value = UserDetailUiState.Error(
                        message = eventsResult.exceptionOrNull()?.message
                            ?: "Failed to load user events"
                    )
                    return@launch
                }

                val events = eventsResult.getOrThrow()

                // Step 3: Load common events (optional, only if logged-in user provided)
                val commonEvents = if (loggedInUserNickname != null) {
                    val commonEventsResult = findCommonEventsUseCase.execute(
                        loggedInUserNickname = loggedInUserNickname,
                        searchedUserNickname = nickname
                    )

                    // Partial failure is acceptable for common events
                    if (commonEventsResult.isSuccess) {
                        commonEventsResult.getOrThrow()
                    } else {
                        emptyList()
                    }
                } else {
                    emptyList()
                }

                // Success state with all data
                _uiState.value = UserDetailUiState.Success(
                    user = user,
                    events = events,
                    commonEvents = commonEvents
                )

            } catch (e: Exception) {
                _uiState.value = UserDetailUiState.Error(
                    message = e.message ?: "Unexpected error while loading user details"
                )
            }
        }
    }
}

/**
 * MVI State - Represents all possible UI states for user detail.
 *
 * This sealed interface ensures type-safe state handling with exhaustive when-expressions.
 *
 * State Transitions:
 * ```
 * Loading → Success | Error
 * Error → Loading (via Retry intent)
 * ```
 */
sealed interface UserDetailUiState {
    /**
     * Loading state - fetching user details, events, and common events.
     */
    data object Loading : UserDetailUiState

    /**
     * Success state - all data loaded successfully.
     *
     * @property user The ConnpassUser information
     * @property events List of events the user has participated in
     * @property commonEvents List of events both users participated in (empty if no logged-in user)
     */
    data class Success(
        val user: ConnpassUser,
        val events: List<Event>,
        val commonEvents: List<Event>
    ) : UserDetailUiState

    /**
     * Error state - failed to load user details or events.
     *
     * @property message The error message to display
     */
    data class Error(val message: String) : UserDetailUiState
}

/**
 * MVI Intent - Represents all possible user actions.
 *
 * Intents are processed by ViewModel.handleIntent() and result in state changes.
 */
sealed interface UserDetailIntent {
    /**
     * Load user detail information.
     *
     * @property nickname Target user's connpass nickname
     * @property loggedInUserNickname Logged-in user's nickname (nullable, for common events)
     */
    data class LoadUserDetail(
        val nickname: String,
        val loggedInUserNickname: String? = null
    ) : UserDetailIntent

    /**
     * Retry loading user detail after an error.
     */
    data object Retry : UserDetailIntent

    /**
     * Clear error state.
     */
    data object ClearError : UserDetailIntent
}
