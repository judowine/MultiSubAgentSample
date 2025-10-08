package org.example.project.judowine.ui.component.organism

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Atomic Design - ORGANISM: Profile Form
 *
 * Implemented by: project-orchestrator (Task 3.9)
 * PBI-1, Task 3.9: Atomic Design component extraction
 *
 * An organism component combining multiple input fields for profile editing.
 * Used in ProfileRegistrationScreen and ProfileEditScreen.
 *
 * **Composition**:
 * - Card (Material3 component)
 * - OutlinedTextField (Material3 component) x2
 *   - Connpass ID field
 *   - Nickname field
 *
 * **Usage**:
 * ```kotlin
 * ProfileForm(
 *     connpassId = "user123",
 *     nickname = "John Doe",
 *     onConnpassIdChange = { /* update state */ },
 *     onNicknameChange = { /* update state */ },
 *     isEnabled = true
 * )
 * ```
 *
 * @param connpassId Current connpass ID value
 * @param nickname Current nickname value
 * @param onConnpassIdChange Callback when connpass ID changes
 * @param onNicknameChange Callback when nickname changes
 * @param modifier Modifier for customization
 * @param isEnabled Whether the fields are enabled (default: true)
 * @param showErrors Whether to show error states (default: false)
 */
@Composable
fun ProfileForm(
    connpassId: String,
    nickname: String,
    onConnpassIdChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    showErrors: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                enabled = isEnabled,
                isError = showErrors && connpassId.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            // Nickname TextField
            OutlinedTextField(
                value = nickname,
                onValueChange = onNicknameChange,
                label = { Text("Nickname") },
                placeholder = { Text("Enter your nickname") },
                singleLine = true,
                enabled = isEnabled,
                isError = showErrors && nickname.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
