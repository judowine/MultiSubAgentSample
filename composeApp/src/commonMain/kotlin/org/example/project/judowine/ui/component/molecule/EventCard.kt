package org.example.project.judowine.ui.component.molecule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import org.example.project.judowine.domain.model.Event

/**
 * Atomic Design - MOLECULE: Event Card
 *
 * Implemented by: compose-ui-architect
 * PBI-2, Task 4.9: Atomic Design component extraction
 *
 * A molecule component displaying event summary information in a card layout.
 * Used in EventListScreen to show event list items.
 *
 * **Composition**:
 * - Card (Material3)
 * - Title Text (Atom)
 * - Date Text (Atom)
 * - Location/Online badge (Atom)
 * - Participant stats row (Atoms)
 * - Event status badge (Atom)
 *
 * **Usage**:
 * ```kotlin
 * EventCard(
 *     event = event,
 *     onClick = { /* Navigate to detail */ }
 * )
 * ```
 *
 * @param event The event to display
 * @param onClick Callback when the card is clicked
 * @param modifier Modifier for customization
 */
@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Date
            Text(
                text = formatEventDate(event.startedAt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Location (if available)
            event.address?.let { address ->
                Text(
                    text = "📍 $address",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } ?: run {
                Text(
                    text = "💻 Online Event",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Participant info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Accepted: ${event.accepted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (event.waiting > 0) {
                    Text(
                        text = "Waiting: ${event.waiting}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                event.limit?.let { limit ->
                    Text(
                        text = "Limit: $limit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Event status indicator
            if (event.isFull()) {
                Text(
                    text = "FULL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.End)
                )
            } else if (event.isUnlimited()) {
                Text(
                    text = "UNLIMITED",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.End)
                )
            } else {
                event.availableSlots()?.let { slots ->
                    Text(
                        text = "$slots slots available",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

/**
 * Utility function to format event date for display.
 *
 * TODO: Replace with proper date formatting using kotlinx-datetime formatting APIs
 * when available in Compose Multiplatform.
 *
 * @param instant The event start time
 * @return Formatted date string
 */
private fun formatEventDate(instant: Instant): String {
    // Basic formatting - replace with kotlinx-datetime formatting when available
    return instant.toString().replace("T", " ").substringBefore(".")
}
