package org.example.project.judowine.ui.common

/**
 * Generic sealed interface representing the three standard UI states.
 *
 * This pattern is used across all screens in the EventMeet application to provide
 * consistent state management for asynchronous operations.
 *
 * Implemented by: project-orchestrator
 * PBI-1, Task 3.6b: Extract generic UiState<T> pattern
 *
 * Design rationale:
 * - Prevents duplication of Loading/Success/Error pattern across screens
 * - Uses covariant type parameter (out T) for flexibility
 * - Loading and Error use Nothing as they don't carry typed data
 * - Success carries generic data of type T
 *
 * Usage example:
 * ```kotlin
 * var state by remember { mutableStateOf<UiState<User>>(UiState.Loading) }
 *
 * when (state) {
 *     is UiState.Loading -> LoadingContent()
 *     is UiState.Success -> SuccessContent(data = state.data)
 *     is UiState.Error -> ErrorContent(message = state.message)
 * }
 * ```
 *
 * @param T The type of data contained in the Success state
 */
sealed interface UiState<out T> {
    /**
     * Loading state - operation is in progress.
     *
     * This state is typically used when:
     * - Fetching data from a repository
     * - Performing async operations
     * - Waiting for user input validation
     */
    data object Loading : UiState<Nothing>

    /**
     * Success state - operation completed successfully.
     *
     * @property data The result data of type T
     */
    data class Success<T>(val data: T) : UiState<T>

    /**
     * Error state - operation failed.
     *
     * @property message The error message to display to the user
     */
    data class Error(val message: String) : UiState<Nothing>
}
