package org.example.project.judowine.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.judowine.domain.model.User
import org.example.project.judowine.domain.usecase.GetUserProfileUseCase
import org.example.project.judowine.domain.usecase.SaveUserProfileUseCase
import org.example.project.judowine.ui.common.UiState

/**
 * ViewModel for Profile Management using MVI (Model-View-Intent) pattern.
 *
 * Implemented by: project-orchestrator (following compose-ui-architect patterns)
 * PBI-1, Task 3.8: ProfileViewModel with MVI pattern
 *
 * This ViewModel manages state for all profile-related screens:
 * - ProfileRegistrationScreen (creation)
 * - ProfileDisplayScreen (viewing)
 * - ProfileEditScreen (editing)
 *
 * MVI Pattern Components:
 * - **Model**: ProfileUiState (sealed interface representing all possible states)
 * - **View**: Composable screens observing stateFlow and emitting intents
 * - **Intent**: ProfileIntent (sealed interface representing user actions)
 *
 * State Flow:
 * ```
 * User Action (Intent) → ViewModel.handleIntent() → Update State → UI observes StateFlow → Re-render
 * ```
 *
 * Design Notes:
 * - Uses Kotlin StateFlow for reactive state management
 * - Single source of truth for profile UI state
 * - Unifies state management from Tasks 3.5, 3.6, 3.7
 * - Follows Android UDF: ViewModel → Use Cases (domain layer)
 * - NO direct data layer access
 *
 * @property getUserProfileUseCase Use case for retrieving user profiles
 * @property saveUserProfileUseCase Use case for saving/updating user profiles
 */
class ProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase
) : ViewModel() {

    // State Management
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /**
     * Handle user intents (actions) and update state accordingly.
     *
     * This is the single entry point for all user interactions.
     *
     * @param intent The user intent to process
     */
    fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.LoadProfile -> loadProfile()
            is ProfileIntent.CreateProfile -> createProfile(intent.connpassId, intent.nickname)
            is ProfileIntent.UpdateProfile -> updateProfile(intent.user, intent.connpassId, intent.nickname)
            is ProfileIntent.ClearError -> clearError()
            is ProfileIntent.Reset -> reset()
        }
    }

    /**
     * Load the primary user profile.
     */
    private fun loadProfile() {
        _uiState.value = ProfileUiState.Loading

        viewModelScope.launch {
            try {
                val user = getUserProfileUseCase.getPrimaryUser()
                _uiState.value = if (user != null) {
                    ProfileUiState.ProfileLoaded(user)
                } else {
                    ProfileUiState.NoProfile
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(
                    message = e.message ?: "Failed to load profile"
                )
            }
        }
    }

    /**
     * Create a new user profile (registration flow).
     *
     * @param connpassId The connpass ID
     * @param nickname The user's nickname
     */
    private fun createProfile(connpassId: String, nickname: String) {
        _uiState.value = ProfileUiState.Saving

        viewModelScope.launch {
            val result = saveUserProfileUseCase.createUserProfile(
                connpassId = connpassId.trim(),
                nickname = nickname.trim()
            )

            result.fold(
                onSuccess = { user ->
                    _uiState.value = ProfileUiState.SaveSuccess(user)
                },
                onFailure = { exception ->
                    _uiState.value = ProfileUiState.Error(
                        message = exception.message ?: "Failed to create profile"
                    )
                }
            )
        }
    }

    /**
     * Update an existing user profile (edit flow).
     *
     * @param user The existing user to update
     * @param connpassId The new connpass ID
     * @param nickname The new nickname
     */
    private fun updateProfile(user: User, connpassId: String, nickname: String) {
        _uiState.value = ProfileUiState.Saving

        viewModelScope.launch {
            val result = saveUserProfileUseCase.saveOrUpdateUserProfile(
                connpassId = connpassId.trim(),
                nickname = nickname.trim(),
                existingUserId = user.id
            )

            result.fold(
                onSuccess = { updatedUser ->
                    _uiState.value = ProfileUiState.SaveSuccess(updatedUser)
                },
                onFailure = { exception ->
                    _uiState.value = ProfileUiState.Error(
                        message = exception.message ?: "Failed to update profile"
                    )
                }
            )
        }
    }

    /**
     * Clear error state and return to idle.
     */
    private fun clearError() {
        _uiState.value = ProfileUiState.Idle
    }

    /**
     * Reset to idle state.
     */
    private fun reset() {
        _uiState.value = ProfileUiState.Idle
    }
}

/**
 * MVI State - Represents all possible UI states for profile management.
 *
 * This sealed interface ensures type-safe state handling with exhaustive when-expressions.
 *
 * State Transitions:
 * ```
 * Idle → Loading → ProfileLoaded | NoProfile | Error
 * Idle → Saving → SaveSuccess | Error
 * Error → Idle (via ClearError intent)
 * ```
 */
sealed interface ProfileUiState {
    /**
     * Idle state - initial state or after reset.
     */
    data object Idle : ProfileUiState

    /**
     * Loading state - fetching user profile.
     */
    data object Loading : ProfileUiState

    /**
     * Profile loaded successfully.
     *
     * @property user The loaded user profile
     */
    data class ProfileLoaded(val user: User) : ProfileUiState

    /**
     * No profile found - user needs to register.
     */
    data object NoProfile : ProfileUiState

    /**
     * Saving state - creating or updating profile.
     */
    data object Saving : ProfileUiState

    /**
     * Save operation completed successfully.
     *
     * @property user The saved/updated user profile
     */
    data class SaveSuccess(val user: User) : ProfileUiState

    /**
     * Error state - operation failed.
     *
     * @property message The error message to display
     */
    data class Error(val message: String) : ProfileUiState
}

/**
 * MVI Intent - Represents all possible user actions.
 *
 * Intents are processed by ViewModel.handleIntent() and result in state changes.
 */
sealed interface ProfileIntent {
    /**
     * Load the primary user profile.
     */
    data object LoadProfile : ProfileIntent

    /**
     * Create a new user profile.
     *
     * @property connpassId The connpass ID
     * @property nickname The user's nickname
     */
    data class CreateProfile(
        val connpassId: String,
        val nickname: String
    ) : ProfileIntent

    /**
     * Update an existing user profile.
     *
     * @property user The existing user to update
     * @property connpassId The new connpass ID
     * @property nickname The new nickname
     */
    data class UpdateProfile(
        val user: User,
        val connpassId: String,
        val nickname: String
    ) : ProfileIntent

    /**
     * Clear error state.
     */
    data object ClearError : ProfileIntent

    /**
     * Reset to idle state.
     */
    data object Reset : ProfileIntent
}
