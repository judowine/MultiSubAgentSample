package org.example.project.judowine.ui.component.organism

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.judowine.domain.model.Event
import org.example.project.judowine.ui.component.molecule.EventCard

/**
 * Atomic Design - ORGANISM: Common Events List
 *
 * Implemented by: compose-ui-architect
 * PBI-3, Tasks 5.6 & 6.5: Extract reusable UI components for user search and detail screens
 *
 * An organism component displaying a list of events with special "Common" badges.
 * Used to highlight events that both the logged-in user and viewed user participated in.
 *
 * **Composition**:
 * - Section header Row (Title text + Count badge)
 * - Description text (Atom)
 * - EventCard molecules with "Common" badge overlay
 *
 * **Usage**:
 * ```kotlin
 * CommonEventsList(
 *     commonEvents = listOf(event1, event2),
 *     onEventClick = { event -> navigateToEventDetail(event) }
 * )
 * ```
 *
 * **Reusability**:
 * - UserDetailScreen: Display common events between users
 * - FriendListScreen (future): Show common events with friends
 * - Any screen comparing user participation
 *
 * **Design Pattern**:
 * - Reuses EventCard from PBI-2 for consistency
 * - Adds visual badge overlay to indicate "common" status
 * - Badge positioned at top-right corner of each card
 *
 * @param commonEvents List of events both users participated in
 * @param onEventClick Callback when an event card is clicked
 * @param modifier Modifier for customization
 */
@Composable
fun CommonEventsList(
    commonEvents: List<Event>,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section Header with Count Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Common Events",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "${commonEvents.size} common",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Description
        Text(
            text = "Events you both participated in",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Common Events Cards with Badge Overlay
        commonEvents.forEach { event ->
            Box(modifier = Modifier.fillMaxWidth()) {
                EventCard(
                    event = event,
                    onClick = { onEventClick(event) }
                )

                // "Common" Badge Overlay
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.tertiary
                ) {
                    Text(
                        text = "⭐ Common",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
