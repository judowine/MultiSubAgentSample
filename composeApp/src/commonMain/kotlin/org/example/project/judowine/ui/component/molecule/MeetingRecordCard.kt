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
import org.example.project.judowine.domain.model.MeetingRecord

/**
 * Atomic Design - MOLECULE: Meeting Record Card
 *
 * Implemented by: compose-ui-architect (coordinated by project-orchestrator)
 * PBI-4, Task 7.10: MeetingRecordCard molecule component
 * PBI-5, Task 9.5: Enhanced with note preview and tags display
 *
 * A molecule component displaying meeting record information in a card layout.
 * Used in MeetingRecordListScreen to show meeting record list items.
 *
 * **Composition**:
 * - Card (Material3)
 * - User avatar/icon (Atom - using UserAvatar or placeholder)
 * - User nickname (primary text - Atom)
 * - Event title placeholder (secondary text - Atom) - Note: eventId displayed until event lookup added
 * - Note preview (if available) - truncated to 50 chars with "..." (PBI-5)
 * - Tags display (if available) - FlowRow with limit to 3 visible + "+N more" (PBI-5)
 * - Meeting date (tertiary text - Atom)
 *
 * **Design Notes**:
 * - Stateless component - receives all data through parameters
 * - Clickable for navigation to detail view
 * - Material3 design with proper elevation and colors
 * - Shows recently created indicator for meetings in last 24 hours
 * - Conditional rendering: note/tags sections only shown if they exist
 *
 * **Usage**:
 * ```kotlin
 * MeetingRecordCard(
 *     meetingRecord = record,
 *     eventTitle = "Event Title", // From event lookup
 *     onClick = { /* Navigate to detail */ }
 * )
 * ```
 *
 * @param meetingRecord The meeting record to display
 * @param eventTitle The title of the event (from event lookup, or null if unavailable)
 * @param onClick Callback when the card is clicked
 * @param modifier Modifier for customization
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MeetingRecordCard(
    meetingRecord: MeetingRecord,
    eventTitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (meetingRecord.isRecentlyCreated()) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // User Avatar/Icon (placeholder for now, can enhance with actual avatar in future)
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👤",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            // Meeting info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // User nickname (primary text)
                Text(
                    text = meetingRecord.nickname,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Event title (secondary text)
                Text(
                    text = eventTitle ?: "Event ID: ${meetingRecord.eventId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Note preview (if available) - PBI-5
                if (meetingRecord.hasNotes()) {
                    Text(
                        text = meetingRecord.notePreview(50) ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Tags display (if available) - PBI-5
                if (meetingRecord.hasTags()) {
                    val visibleTagLimit = 3
                    val visibleTags = meetingRecord.tags.take(visibleTagLimit)
                    val remainingTagsCount = meetingRecord.tags.size - visibleTagLimit

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        visibleTags.forEach { tagName ->
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = tagName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Show "+N more" if there are more tags
                        if (remainingTagsCount > 0) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    text = "+$remainingTagsCount more",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Meeting date (tertiary text)
                Text(
                    text = formatMeetingDate(meetingRecord.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Recently created indicator
            if (meetingRecord.isRecentlyCreated()) {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "New",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Utility function to format meeting date for display.
 *
 * Shows relative time for recent meetings, absolute date for older ones.
 *
 * TODO: Replace with proper date formatting using kotlinx-datetime formatting APIs
 * when available in Compose Multiplatform.
 *
 * @param instant The meeting creation time
 * @return Formatted date string
 */
private fun formatMeetingDate(instant: Instant): String {
    // Basic formatting - replace with kotlinx-datetime formatting when available
    val dateStr = instant.toString().replace("T", " ").substringBefore(".")
    return "Met on: $dateStr"
}
