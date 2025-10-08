package org.example.project.judowine.ui.component.organism

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.judowine.domain.model.Event
import org.example.project.judowine.ui.component.molecule.EventStatusCard
import org.example.project.judowine.ui.component.molecule.ParticipantStatCard

/**
 * Atomic Design - ORGANISM: Event Participants Section
 *
 * Implemented by: compose-ui-architect
 * PBI-2, Task 4.9: Atomic Design component extraction
 *
 * An organism component displaying participant statistics and availability status:
 * - Section title
 * - Participant stat cards (Accepted, Waiting, Limit)
 * - Availability status card (FULL/UNLIMITED/available slots)
 *
 * **Composition**:
 * - Section title Text (Atom)
 * - ParticipantStatCard molecules (for accepted, waiting, limit)
 * - EventStatusCard molecule (for availability)
 *
 * **Usage**:
 * ```kotlin
 * EventParticipantsSection(event = event)
 * ```
 *
 * @param event The event to display
 * @param modifier Modifier for customization
 */
@Composable
fun EventParticipantsSection(
    event: Event,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section Title
        Text(
            text = "Participants",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Participant counts row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ParticipantStatCard(
                label = "Accepted",
                count = event.accepted,
                color = MaterialTheme.colorScheme.primary
            )

            if (event.waiting > 0) {
                ParticipantStatCard(
                    label = "Waiting",
                    count = event.waiting,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            event.limit?.let { limit ->
                ParticipantStatCard(
                    label = "Limit",
                    count = limit,
                    color = MaterialTheme.colorScheme.secondary
                )
            } ?: run {
                ParticipantStatCard(
                    label = "Limit",
                    count = null,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Availability status
        Spacer(modifier = Modifier.height(8.dp))

        EventStatusCard(event = event)
    }
}
