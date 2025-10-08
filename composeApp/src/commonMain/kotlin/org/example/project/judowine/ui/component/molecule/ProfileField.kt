package org.example.project.judowine.ui.component.molecule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Atomic Design - MOLECULE: Profile Field
 *
 * Implemented by: project-orchestrator (Task 3.9)
 * PBI-1, Task 3.9: Atomic Design component extraction
 *
 * A molecule component displaying a label-value pair for profile information.
 * Used in ProfileDisplayScreen to show user profile details.
 *
 * **Composition**:
 * - Label Text (Atom)
 * - Value Text (Atom)
 *
 * **Usage**:
 * ```kotlin
 * ProfileField(
 *     label = "Connpass ID",
 *     value = "user123"
 * )
 * ```
 *
 * @param label The field label (e.g., "Connpass ID", "Nickname")
 * @param value The field value
 * @param modifier Modifier for customization
 */
@Composable
fun ProfileField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Value
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
