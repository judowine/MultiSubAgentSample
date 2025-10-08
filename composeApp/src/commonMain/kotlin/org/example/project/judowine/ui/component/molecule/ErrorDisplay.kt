package org.example.project.judowine.ui.component.molecule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Atomic Design - MOLECULE: Error Display
 *
 * Implemented by: project-orchestrator (Task 3.9)
 * PBI-1, Task 3.9: Atomic Design component extraction
 *
 * A molecule component displaying an error icon and message.
 * Used in ProfileDisplayScreen and ProfileEditScreen error states.
 *
 * **Composition**:
 * - Error Icon (Text emoji - can be replaced with Icon atom)
 * - Error Message Text (Atom)
 *
 * **Usage**:
 * ```kotlin
 * ErrorDisplay(
 *     message = "Failed to load profile",
 *     modifier = Modifier.padding(16.dp)
 * )
 * ```
 *
 * @param message The error message to display
 * @param modifier Modifier for customization
 * @param icon The icon to display (default: warning emoji)
 */
@Composable
fun ErrorDisplay(
    message: String,
    modifier: Modifier = Modifier,
    icon: String = "⚠️"
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Error Icon (using Text as placeholder - can be replaced with Icon)
        Text(
            text = icon,
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
    }
}
