package org.example.project.judowine.ui.component.organism

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import org.example.project.judowine.domain.model.Event
import org.example.project.judowine.ui.component.molecule.ProfileField

/**
 * Atomic Design - ORGANISM: Event Header Section
 *
 * Implemented by: compose-ui-architect
 * PBI-2, Task 4.9: Atomic Design component extraction
 *
 * An organism component displaying the header information for an event detail view:
 * - Event title
 * - Start date/time
 * - End date/time (if available)
 * - Location/address
 * - Online event badge (if applicable)
 *
 * **Composition**:
 * - Title Text (Atom)
 * - ProfileField molecules (for date/time and location)
 * - Badge (Material3) for online events
 *
 * **Usage**:
 * ```kotlin
 * EventHeaderSection(event = event)
 * ```
 *
 * @param event The event to display
 * @param modifier Modifier for customization
 */
@Composable
fun EventHeaderSection(
    event: Event,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title
        Text(
            text = event.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Start Date/Time
        ProfileField(
            label = "Starts",
            value = formatEventDateTime(event.startedAt)
        )

        // End Date/Time (if available)
        event.endedAt?.let { endTime ->
            ProfileField(
                label = "Ends",
                value = formatEventDateTime(endTime)
            )
        }

        // Location
        ProfileField(
            label = "Location",
            value = event.address ?: "Online Event (no physical venue)"
        )

        // Event Type Badge
        if (event.isOnlineEvent()) {
            Badge {
                Text(
                    text = "ONLINE",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/**
 * Utility function to format event date/time for display.
 *
 * TODO: Replace with proper date formatting using kotlinx-datetime formatting APIs
 * when available in Compose Multiplatform.
 *
 * @param instant The event time
 * @return Formatted date/time string
 */
private fun formatEventDateTime(instant: Instant): String {
    // Basic formatting - replace with kotlinx-datetime formatting when available
    return instant.toString().replace("T", " at ").substringBefore(".")
}
