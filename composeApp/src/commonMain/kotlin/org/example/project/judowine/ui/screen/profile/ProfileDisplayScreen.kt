package org.example.project.judowine.ui.screen.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.project.judowine.domain.model.User
import org.example.project.judowine.domain.usecase.GetUserProfileUseCase
import org.example.project.judowine.ui.common.UiState

/**
 * Profile Display Screen for EventMeet application.
 *
 * Implemented by: compose-ui-architect (via project-orchestrator)
 * PBI-1, Task 3.6: ProfileDisplayScreen
 *
 * This screen displays the user's profile information including:
 * - Connpass ID
 * - Nickname
 * - Created timestamp
 * - Updated timestamp
 *
 * The screen handles loading and error states appropriately.
 *
 * Design Notes:
 * - Follows Screen/Content pattern from Task 3.5 (ProfileRegistrationScreen)
 * - Uses local state management (will be refactored to ViewModel in Task 3.8)
 * - Designed for easy integration with MVI pattern
 * - Stateless components ready for Atomic Design refactoring (Task 3.9)
 * - Follows Material3 design guidelines
 * - Follows Android UDF: composeApp → shared (domain) - NO direct data layer access
 *
 * @param getUserProfileUseCase Use case for retrieving user profiles (from domain layer)
 * @param onEditProfile Callback when edit profile button is clicked
 * @param modifier Optional modifier for customization
 */
@Composable
fun ProfileDisplayScreen(
    getUserProfileUseCase: GetUserProfileUseCase,
    onEditProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // State management - will be moved to ViewModel in Task 3.8
    var displayState by remember { mutableStateOf<UiState<User>>(UiState.Loading) }

    val coroutineScope = rememberCoroutineScope()

    // Load user profile on screen entry
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val user = getUserProfileUseCase.getPrimaryUser()
                displayState = if (user != null) {
                    UiState.Success(user)
                } else {
                    UiState.Error("No user profile found. Please register first.")
                }
            } catch (e: Exception) {
                displayState = UiState.Error(
                    e.message ?: "Failed to load profile"
                )
            }
        }
    }

    ProfileDisplayContent(
        state = displayState,
        onEditProfileClick = onEditProfile,
        onRetryClick = {
            // Reload profile
            displayState = UiState.Loading
            coroutineScope.launch {
                try {
                    val user = getUserProfileUseCase.getPrimaryUser()
                    displayState = if (user != null) {
                        UiState.Success(user)
                    } else {
                        UiState.Error("No user profile found. Please register first.")
                    }
                } catch (e: Exception) {
                    displayState = UiState.Error(
                        e.message ?: "Failed to load profile"
                    )
                }
            }
        },
        modifier = modifier
    )
}

/**
 * Stateless content composable for Profile Display.
 *
 * This component is designed to be reusable and testable. It receives all state
 * as parameters and emits events through callbacks (stateless pattern).
 *
 * This design will facilitate:
 * - Easy integration with ProfileViewModel (Task 3.8)
 * - Extraction of reusable atomic components (Task 3.9)
 * - Unit testing without complex setup
 *
 * @param state The current display state (Loading, Success, or Error)
 * @param onEditProfileClick Callback when edit profile button is clicked
 * @param onRetryClick Callback when retry button is clicked (in error state)
 * @param modifier Optional modifier for customization
 */
@Composable
fun ProfileDisplayContent(
    state: UiState<User>,
    onEditProfileClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (state) {
            is UiState.Loading -> {
                LoadingContent()
            }
            is UiState.Success -> {
                SuccessContent(
                    user = state.data,
                    onEditProfileClick = onEditProfileClick
                )
            }
            is UiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetryClick = onRetryClick
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
        CircularProgressIndicator()
    }
}

/**
 * Success state content - displays user profile information.
 *
 * @param user The user profile to display
 * @param onEditProfileClick Callback when edit button is clicked
 */
@Composable
private fun SuccessContent(
    user: User,
    onEditProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .safeContentPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Your Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Profile Information Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Connpass ID
                ProfileField(
                    label = "Connpass ID",
                    value = user.connpassId
                )

                HorizontalDivider()

                // Nickname
                ProfileField(
                    label = "Nickname",
                    value = user.nickname
                )

                HorizontalDivider()

                // Created At
                ProfileField(
                    label = "Registered",
                    value = user.createdAt.toString()
                )

                HorizontalDivider()

                // Updated At
                ProfileField(
                    label = "Last Updated",
                    value = user.updatedAt.toString()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Edit Profile Button
        Button(
            onClick = onEditProfileClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp)
        ) {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Profile field component - displays a label and value pair.
 *
 * @param label The field label
 * @param value The field value
 */
@Composable
private fun ProfileField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Error state content - displays an error message with retry button.
 *
 * @param message The error message to display
 * @param onRetryClick Callback when retry button is clicked
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .safeContentPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Error Icon (using Text as placeholder - can be replaced with Icon)
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Error Message
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Retry Button
        Button(
            onClick = onRetryClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp)
        ) {
            Text(
                text = "Retry",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
