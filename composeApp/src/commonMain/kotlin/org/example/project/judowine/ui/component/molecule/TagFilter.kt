package org.example.project.judowine.ui.component.molecule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Molecule component for displaying tag filter chips.
 *
 * Implementation by: compose-ui-architect (coordinated by project-orchestrator)
 * PBI-6, Task 8: TagFilter molecule component
 *
 * This component follows Atomic Design methodology (Molecule level).
 * TagFilter displays a row of filter chips allowing users to filter content by tag.
 *
 * Usage:
 * ```kotlin
 * TagFilter(
 *     availableTags = listOf("kotlin", "architecture", "backend"),
 *     selectedTag = "kotlin", // null means "All" is selected
 *     onTagSelected = { tagName -> /* filter by tag */ }
 * )
 * ```
 *
 * Design:
 * - Material3 FilterChip components
 * - "All" option to clear filter (selectedTag = null)
 * - Selected tag is highlighted
 * - Stateless component (parent controls selected state)
 * - FlowRow layout for wrapping chips
 *
 * @param availableTags List of unique tag names available for filtering
 * @param selectedTag Currently selected tag name (null if "All" is selected)
 * @param onTagSelected Callback when a tag is selected (null for "All")
 * @param modifier Modifier for customization (optional)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagFilter(
    availableTags: List<String>,
    selectedTag: String?,
    onTagSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" filter chip (clears filter)
        FilterChip(
            selected = selectedTag == null,
            onClick = { onTagSelected(null) },
            label = {
                Text(
                    text = "All",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        )

        // Tag filter chips
        availableTags.forEach { tag ->
            FilterChip(
                selected = selectedTag == tag,
                onClick = { onTagSelected(tag) },
                label = {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
        }
    }
}
