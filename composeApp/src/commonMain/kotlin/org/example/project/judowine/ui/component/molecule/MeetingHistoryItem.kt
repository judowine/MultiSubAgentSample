package org.example.project.judowine.ui.component.molecule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Molecule component for displaying a single meeting history item in a timeline.
 *
 * Implementation by: compose-ui-architect (coordinated by project-orchestrator)
 * PBI-6, Task 7: MeetingHistoryItem molecule component
 *
 * This component follows Atomic Design methodology (Molecule level).
 * A MeetingHistoryItem displays summary information about a single meeting record
 * in the person's meeting history timeline.
 *
 * Usage:
 * ```kotlin
 * MeetingHistoryItem(
 *     eventTitle = "Kotlin Conf 2025",
 *     eventDate = Clock.System.now(),
 *     notesPreview = "Discussed KMP architecture...",
 *     tags = listOf("kotlin", "architecture"),
 *     onEventClick = { /* navigate to event detail */ },
 *     onMeetingClick = { /* navigate to meeting record detail */ }
 * )
 * ```
 *
 * Design:
 * - Material3 Card
 * - Clickable for navigation to MeetingRecordDetailScreen
 * - Event title is also clickable (navigates to EventDetailScreen)
 * - Displays: event title, date, notes preview, tags
 * - Stateless component (no internal state)
 *
 * @param eventTitle Title of the event where the meeting occurred
 * @param eventDate Date of the event
 * @param notesPreview Preview of the meeting notes (optional, null if no notes)
 * @param tags List of tag names associated with this meeting
 * @param onEventClick Callback when event title is clicked (navigate to EventDetailScreen)
 * @param onMeetingClick Callback when card is clicked (navigate to MeetingRecordDetailScreen)
 * @param modifier Modifier for customization (optional)
 */
@Composable
fun MeetingHistoryItem(
    eventTitle: String,
    eventDate: Instant,
    notesPreview: String?,
    tags: List<String>,
    onEventClick: () -> Unit,
    onMeetingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onMeetingClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Event title (clickable separately)
            Text(
                text = eventTitle,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onEventClick),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Event date
            Text(
                text = formatDate(eventDate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Notes preview (if exists)
            if (!notesPreview.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notesPreview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Tags (if exist)
            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.take(3).forEach { tag ->
                        AssistChip(
                            onClick = { /* Tag chips are read-only in this view */ },
                            label = {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                    if (tags.size > 3) {
                        Text(
                            text = "+${tags.size - 3} more",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }
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
