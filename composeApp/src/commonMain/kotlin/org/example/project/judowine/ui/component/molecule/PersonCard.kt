package org.example.project.judowine.ui.component.molecule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Molecule component for displaying a person summary card.
 *
 * Implementation by: compose-ui-architect (coordinated by project-orchestrator)
 * PBI-6, Task 6: PersonCard molecule component
 *
 * This component follows Atomic Design methodology (Molecule level).
 * A PersonCard displays summary information about a person the user has met,
 * including their nickname, number of meetings, and most recent meeting date.
 *
 * Usage:
 * ```kotlin
 * PersonCard(
 *     nickname = "John Doe",
 *     meetingCount = 3,
 *     lastMeetingDate = Clock.System.now(),
 *     onClick = { /* navigate to person detail */ }
 * )
 * ```
 *
 * Design:
 * - Material3 Card with elevation
 * - Clickable for navigation to PersonDetailScreen
 * - Stateless component (no internal state)
 * - Displays: nickname (title), meeting count, last meeting date
 *
 * @param nickname Display name of the person
 * @param meetingCount Total number of meetings with this person
 * @param lastMeetingDate Date of the most recent meeting
 * @param onClick Callback when the card is clicked (for navigation)
 * @param modifier Modifier for customization (optional)
 */
@Composable
fun PersonCard(
    nickname: String,
    meetingCount: Int,
    lastMeetingDate: Instant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Nickname and meeting count
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = nickname,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$meetingCount meeting${if (meetingCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right: Last meeting date
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Last met",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(lastMeetingDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Format Instant to human-readable date string.
 * Example: "2025-10-09"
 *
 * @param instant The instant to format
 * @return Formatted date string
 */
private fun formatDate(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.year}-${localDateTime.monthNumber.toString().padStart(2, '0')}-${localDateTime.dayOfMonth.toString().padStart(2, '0')}"
}
