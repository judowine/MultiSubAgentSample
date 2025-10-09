package org.example.project.judowine.ui.component.organism

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Atomic Design - ORGANISM: Empty User Search State
 *
 * Implemented by: compose-ui-architect
 * PBI-3, Tasks 5.6 & 6.5: Extract reusable UI components for user search and detail screens
 *
 * An organism component displaying an empty state message when no users are found.
 * Provides friendly user feedback with icon, title, and description.
 *
 * **Composition**:
 * - Emoji icon (Atom) - Large decorative emoji
 * - Title text (Atom) - "No users found"
 * - Description text (Atom) - Helpful suggestion
 *
 * **Usage**:
 * ```kotlin
 * // Default empty state (no results)
 * EmptyUserSearchState()
 *
 * // Idle state (no search performed yet)
 * EmptyUserSearchState(
 *     icon = "🔍",
 *     title = "Search for connpass users",
 *     description = "Enter a nickname to find users"
 * )
 * ```
 *
 * **Reusability**:
 * - UserSearchScreen: Empty and Idle states
 * - Any search screen requiring empty state feedback
 * - Generic empty state display with customizable messages
 *
 * @param icon Emoji icon to display (default: 👤)
 * @param title Title message (default: "No users found")
 * @param description Description message (default: "Try a different search term")
 * @param modifier Modifier for customization
 */
@Composable
fun EmptyUserSearchState(
    icon: String = "👤",
    title: String = "No users found",
    description: String = "Try a different search term",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Text(
                text = icon,
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center
            )

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}
