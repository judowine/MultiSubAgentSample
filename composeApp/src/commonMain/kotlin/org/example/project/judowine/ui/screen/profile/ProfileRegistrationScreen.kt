package org.example.project.judowine.ui.screen.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.project.judowine.domain.usecase.SaveUserProfileUseCase

/**
 * Profile Registration Screen for EventMeet application.
 *
 * Implemented by: compose-ui-architect
 * PBI-1, Task 3.5: ProfileRegistrationScreen
 *
 * This screen allows new users to register their profile by entering:
 * - Connpass ID (required)
 * - Nickname (required)
 *
 * The screen includes input validation and persists data via SaveUserProfileUseCase.
 *
 * Design Notes:
 * - Uses local state management (will be refactored to ViewModel in Task 3.8)
 * - Designed for easy integration with MVI pattern
 * - Stateless components ready for Atomic Design refactoring (Task 3.9)
 * - Follows Material3 design guidelines
 * - Follows Android UDF: composeApp → shared (domain) - NO direct data layer access
 *
 * @param saveUserProfileUseCase Use case for saving user profiles (from domain layer)
 * @param onRegistrationSuccess Callback when registration completes successfully
 * @param modifier Optional modifier for customization
 */
@Composable
fun ProfileRegistrationScreen(
    saveUserProfileUseCase: SaveUserProfileUseCase,
    onRegistrationSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State management - will be moved to ViewModel in Task 3.8
    var connpassId by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    ProfileRegistrationContent(
        connpassId = connpassId,
        nickname = nickname,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onConnpassIdChange = { connpassId = it },
        onNicknameChange = { nickname = it },
        onCreateProfileClick = {
            // Clear previous errors
            errorMessage = null

            // Client-side validation
            when {
                connpassId.isBlank() -> {
                    errorMessage = "Connpass ID cannot be empty"
                }
                nickname.isBlank() -> {
                    errorMessage = "Nickname cannot be empty"
                }
                else -> {
                    // Proceed with registration
                    isLoading = true
                    coroutineScope.launch {
                        val result = saveUserProfileUseCase.createUserProfile(
                            connpassId = connpassId.trim(),
                            nickname = nickname.trim()
                        )

                        isLoading = false

                        result.fold(
                            onSuccess = {
                                // Success - navigate to next screen
                                onRegistrationSuccess()
                            },
                            onFailure = { exception ->
                                // Display error from use case
                                errorMessage = exception.message ?: "Registration failed"
                            }
                        )
                    }
                }
            }
        },
        modifier = modifier
    )
}

/**
 * Stateless content composable for Profile Registration.
 *
 * This component is designed to be reusable and testable. It receives all state
 * as parameters and emits events through callbacks (stateless pattern).
 *
 * This design will facilitate:
 * - Easy integration with ProfileViewModel (Task 3.8)
 * - Extraction of reusable atomic components (Task 3.9)
 * - Unit testing without complex setup
 *
 * @param connpassId Current connpass ID input value
 * @param nickname Current nickname input value
 * @param isLoading Whether a registration operation is in progress
 * @param errorMessage Optional error message to display
 * @param onConnpassIdChange Callback when connpass ID changes
 * @param onNicknameChange Callback when nickname changes
 * @param onCreateProfileClick Callback when create profile button is clicked
 * @param modifier Optional modifier for customization
 */
@Composable
fun ProfileRegistrationContent(
    connpassId: String,
    nickname: String,
    isLoading: Boolean,
    errorMessage: String?,
    onConnpassIdChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onCreateProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .safeContentPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "Create Your Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Enter your connpass ID and nickname to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Connpass ID TextField
            OutlinedTextField(
                value = connpassId,
                onValueChange = onConnpassIdChange,
                label = { Text("Connpass ID") },
                placeholder = { Text("Enter your connpass ID") },
                singleLine = true,
                enabled = !isLoading,
                isError = errorMessage != null && connpassId.isBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nickname TextField
            OutlinedTextField(
                value = nickname,
                onValueChange = onNicknameChange,
                label = { Text("Nickname") },
                placeholder = { Text("Enter your nickname") },
                singleLine = true,
                enabled = !isLoading,
                isError = errorMessage != null && nickname.isBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

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

            // Create Profile Button
            Button(
                onClick = onCreateProfileClick,
                enabled = !isLoading,
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
                        text = "Create Profile",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
