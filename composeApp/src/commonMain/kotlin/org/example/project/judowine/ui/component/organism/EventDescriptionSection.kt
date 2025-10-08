package org.example.project.judowine.ui.component.organism

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.judowine.domain.model.Event

/**
 * Atomic Design - ORGANISM: Event Description Section
 *
 * Implemented by: compose-ui-architect
 * PBI-2, Task 4.9: Atomic Design component extraction
 *
 * An organism component displaying the event's full description:
 * - Section title
 * - Description text in a card container
 *
 * **Composition**:
 * - Section title Text (Atom)
 * - Card (Material3)
 * - Description Text (Atom)
 *
 * **Usage**:
 * ```kotlin
 * EventDescriptionSection(event = event)
 * ```
 *
 * @param event The event to display
 * @param modifier Modifier for customization
 */
@Composable
fun EventDescriptionSection(
    event: Event,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section Title
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Description Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = event.description ?: "No description available.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
