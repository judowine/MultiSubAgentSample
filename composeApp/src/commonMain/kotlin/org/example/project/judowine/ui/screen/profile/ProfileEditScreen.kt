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
import org.example.project.judowine.domain.usecase.SaveUserProfileUseCase
import org.example.project.judowine.ui.common.UiState

/**
 * Profile Edit Screen for EventMeet application.
 *
 * Implemented by: project-orchestrator (delegated to compose-ui-architect pattern)
 * PBI-1, Task 3.7: ProfileEditScreen
 *
 * This screen allows users to edit their profile information including:
 * - Connpass ID
 * - Nickname
 *
 * The screen handles loading and error states appropriately.
 *
 * Design Notes:
 * - Follows Screen/Content pattern from Tasks 3.5 and 3.6
 * - Uses UiState<User> for state management (from Task 3.6b)
 * - Uses local state management (will be refactored to ViewModel in Task 3.8)
 * - Designed for easy integration with MVI pattern
 * - Stateless components ready for Atomic Design refactoring (Task 3.9)
 * - Follows Material3 design guidelines
 * - Follows Android UDF: composeApp → shared (domain) - NO direct data layer access
 *
 * @param getUserProfileUseCase Use case for retrieving user profiles (from domain layer)
 * @param saveUserProfileUseCase Use case for saving user profiles (from domain layer)
 * @param onEditSuccess Callback when profile edit completes successfully
 * @param onNavigateBack Callback when back button is clicked
 * @param modifier Optional modifier for customization
 */
@Composable
fun ProfileEditScreen(
    getUserProfileUseCase: GetUserProfileUseCase,
    saveUserProfileUseCase: SaveUserProfileUseCase,
    onEditSuccess: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // State management - will be moved to ViewModel in Task 3.8
    var loadState by remember { mutableStateOf<UiState<User>>(UiState.Loading) }
    var connpassId by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Track original values to detect changes
    var originalUser by remember { mutableStateOf<User?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Load user profile on screen entry
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val user = getUserProfileUseCase.getPrimaryUser()
                if (user != null) {
                    loadState = UiState.Success(user)
                    originalUser = user
                    connpassId = user.connpassId
                    nickname = user.nickname
                } else {
                    loadState = UiState.Error("No user profile found. Please register first.")
                }
            } catch (e: Exception) {
                loadState = UiState.Error(
                    e.message ?: "Failed to load profile"
                )
            }
        }
    }

    // Derived state: Save button enabled when fields are valid and changed
    val isSaveEnabled = remember(connpassId, nickname, originalUser, isSaving) {
        !isSaving &&
        connpassId.isNotBlank() &&
        nickname.isNotBlank() &&
        originalUser != null &&
        (connpassId != originalUser?.connpassId || nickname != originalUser?.nickname)
    }

    ProfileEditContent(
        state = loadState,
        connpassId = connpassId,
        nickname = nickname,
        isLoading = isSaving,
        isSaveEnabled = isSaveEnabled,
        errorMessage = errorMessage,
        onConnpassIdChange = { connpassId = it },
        onNicknameChange = { nickname = it },
        onSaveClick = {
            val user = originalUser
            if (user != null) {
                // Clear previous errors
                errorMessage = null
                isSaving = true

                coroutineScope.launch {
                    val result = saveUserProfileUseCase.saveOrUpdateUserProfile(
                        connpassId = connpassId.trim(),
                        nickname = nickname.trim(),
                        existingUserId = user.id
                    )

                    isSaving = false

                    result.fold(
                        onSuccess = {
                            // Success - navigate back
                            onEditSuccess()
                        },
                        onFailure = { exception ->
                            // Display error
                            errorMessage = exception.message ?: "Failed to save profile"
                        }
                    )
                }
            }
        },
        onBackClick = onNavigateBack,
        modifier = modifier
    )
}

/**
 * Stateless content composable for Profile Edit.
 *
 * This component is designed to be reusable and testable. It receives all state
 * as parameters and emits events through callbacks (stateless pattern).
 *
 * This design will facilitate:
 * - Easy integration with ProfileViewModel (Task 3.8)
 * - Extraction of reusable atomic components (Task 3.9)
 * - Unit testing without complex setup
 *
 * @param state The current load state (Loading, Success, or Error)
 * @param connpassId Current connpass ID input value
 * @param nickname Current nickname input value
 * @param isLoading Whether a save operation is in progress
 * @param isSaveEnabled Whether the save button should be enabled
 * @param errorMessage Optional error message to display
 * @param onConnpassIdChange Callback when connpass ID changes
 * @param onNicknameChange Callback when nickname changes
 * @param onSaveClick Callback when save button is clicked
 * @param onBackClick Callback when back button is clicked
 * @param modifier Optional modifier for customization
 */
@Composable
fun ProfileEditContent(
    state: UiState<User>,
    connpassId: String,
    nickname: String,
    isLoading: Boolean,
    isSaveEnabled: Boolean,
    errorMessage: String?,
    onConnpassIdChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
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
                EditFormContent(
                    connpassId = connpassId,
                    nickname = nickname,
                    isLoading = isLoading,
                    isSaveEnabled = isSaveEnabled,
                    errorMessage = errorMessage,
                    onConnpassIdChange = onConnpassIdChange,
                    onNicknameChange = onNicknameChange,
                    onSaveClick = onSaveClick,
                    onBackClick = onBackClick
                )
            }
            is UiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onBackClick = onBackClick
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
 * Edit form content - displays editable profile fields.
 *
 * @param connpassId Current connpass ID value
 * @param nickname Current nickname value
 * @param isLoading Whether save operation is in progress
 * @param isSaveEnabled Whether save button should be enabled
 * @param errorMessage Optional error message
 * @param onConnpassIdChange Callback when connpass ID changes
 * @param onNicknameChange Callback when nickname changes
 * @param onSaveClick Callback when save button is clicked
 * @param onBackClick Callback when back button is clicked
 */
@Composable
private fun EditFormContent(
    connpassId: String,
    nickname: String,
    isLoading: Boolean,
    isSaveEnabled: Boolean,
    errorMessage: String?,
    onConnpassIdChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
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
            text = "Edit Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "Update your connpass ID and nickname",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Edit Form Card
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
                // Connpass ID TextField
                OutlinedTextField(
                    value = connpassId,
                    onValueChange = onConnpassIdChange,
                    label = { Text("Connpass ID") },
                    placeholder = { Text("Enter your connpass ID") },
                    singleLine = true,
                    enabled = !isLoading,
                    isError = connpassId.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Nickname TextField
                OutlinedTextField(
                    value = nickname,
                    onValueChange = onNicknameChange,
                    label = { Text("Nickname") },
                    placeholder = { Text("Enter your nickname") },
                    singleLine = true,
                    enabled = !isLoading,
                    isError = nickname.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Error Message Display
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Save Button
        Button(
            onClick = onSaveClick,
            enabled = isSaveEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Save Changes",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cancel Button
        TextButton(
            onClick = onBackClick,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp)
        ) {
            Text(
                text = "Cancel",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Error state content - displays an error message with back button.
 *
 * @param message The error message to display
 * @param onBackClick Callback when back button is clicked
 */
@Composable
private fun ErrorContent(
    message: String,
    onBackClick: () -> Unit,
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

        // Back Button
        Button(
            onClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp)
        ) {
            Text(
                text = "Go Back",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
