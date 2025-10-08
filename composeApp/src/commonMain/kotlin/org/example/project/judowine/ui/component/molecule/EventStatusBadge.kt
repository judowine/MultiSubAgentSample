package org.example.project.judowine.ui.component.molecule

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
 * Atomic Design - MOLECULE: Event Status Badge
 *
 * Implemented by: compose-ui-architect
 * PBI-2, Task 4.9: Atomic Design component extraction
 *
 * A molecule component displaying event availability status:
 * - "FULL" (when event has reached capacity)
 * - "UNLIMITED" (when event has no capacity limit)
 * - "{N} slots available" (when event has available slots)
 * - No badge (when status cannot be determined)
 *
 * **Composition**:
 * - Text (Atom) with conditional styling
 *
 * **Usage**:
 * ```kotlin
 * EventStatusBadge(event = event)
 * ```
 *
 * @param event The event to check status for
 * @param modifier Modifier for customization
 */
@Composable
fun EventStatusBadge(
    event: Event,
    modifier: Modifier = Modifier
) {
    when {
        event.isFull() -> {
            Text(
                text = "FULL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = modifier
            )
        }
        event.isUnlimited() -> {
            Text(
                text = "UNLIMITED",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = modifier
            )
        }
        else -> {
            event.availableSlots()?.let { slots ->
                Text(
                    text = "$slots slots available",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = modifier
                )
            }
        }
    }
}

/**
 * Variant of EventStatusBadge that displays status in a card layout.
 *
 * Used in EventDetailScreen to show availability status with more visual prominence.
 *
 * **Usage**:
 * ```kotlin
 * EventStatusCard(event = event)
 * ```
 *
 * @param event The event to check status for
 * @param modifier Modifier for customization
 */
@Composable
fun EventStatusCard(
    event: Event,
    modifier: Modifier = Modifier
) {
    when {
        event.isFull() -> {
            Card(
                modifier = modifier,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "This event is FULL",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        event.isUnlimited() -> {
            Card(
                modifier = modifier,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "Unlimited capacity - everyone can join!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        else -> {
            event.availableSlots()?.let { slots ->
                Card(
                    modifier = modifier,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Text(
                        text = "$slots slots available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
