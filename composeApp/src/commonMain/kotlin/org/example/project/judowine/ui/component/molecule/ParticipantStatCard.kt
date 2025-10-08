package org.example.project.judowine.ui.component.molecule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Atomic Design - MOLECULE: Participant Stat Card
 *
 * Implemented by: compose-ui-architect
 * PBI-2, Task 4.9: Atomic Design component extraction
 *
 * A molecule component displaying a single participant statistic (e.g., Accepted, Waiting, Limit)
 * in a visually distinct card with color theming.
 *
 * **Composition**:
 * - Card (Material3)
 * - Count Text (Atom) - displays number or infinity symbol
 * - Label Text (Atom)
 *
 * **Usage**:
 * ```kotlin
 * ParticipantStatCard(
 *     label = "Accepted",
 *     count = 25,
 *     color = MaterialTheme.colorScheme.primary
 * )
 *
 * // For unlimited
 * ParticipantStatCard(
 *     label = "Limit",
 *     count = null,
 *     color = MaterialTheme.colorScheme.secondary
 * )
 * ```
 *
 * @param label The statistic label (e.g., "Accepted", "Waiting", "Limit")
 * @param count The count value (null for unlimited, displays ∞)
 * @param color The color theme for the card (used for background tint and text)
 * @param modifier Modifier for customization
 */
@Composable
fun ParticipantStatCard(
    label: String,
    count: Int?,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Count or infinity symbol
            Text(
                text = count?.toString() ?: "∞",
                style = MaterialTheme.typography.headlineSmall,
                color = color
            )

            // Label
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
    }
}
