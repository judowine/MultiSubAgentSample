package org.example.project.judowine.ui.component.molecule

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Atomic Design - MOLECULE: Loading Button
 *
 * Implemented by: project-orchestrator (Task 3.9)
 * PBI-1, Task 3.9: Atomic Design component extraction
 *
 * A button that can display a loading indicator or text label.
 * Used in ProfileRegistrationScreen and ProfileEditScreen for save operations.
 *
 * **Composition**:
 * - Button (Material3 component)
 * - Text (Atom) OR CircularProgressIndicator (Atom)
 *
 * **Usage**:
 * ```kotlin
 * LoadingButton(
 *     text = "Save Profile",
 *     isLoading = false,
 *     onClick = { /* save action */ },
 *     enabled = true
 * )
 *
 * LoadingButton(
 *     text = "Saving...",
 *     isLoading = true,
 *     onClick = { /* disabled during loading */ },
 *     enabled = false
 * )
 * ```
 *
 * @param text The button text label
 * @param isLoading Whether to show loading indicator instead of text
 * @param onClick Callback when button is clicked
 * @param modifier Modifier for customization
 * @param enabled Whether the button is enabled (default: true)
 */
@Composable
fun LoadingButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
