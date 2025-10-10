package org.example.project.judowine.ui.component.molecule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.project.judowine.domain.model.ConnpassUser
import org.example.project.judowine.ui.component.atom.UserAvatar

/**
 * Atomic Design - MOLECULE: User Card
 *
 * Implemented by: compose-ui-architect
 * PBI-3, Tasks 5.6 & 6.5: Extract reusable UI components for user search and detail screens
 *
 * A molecule component displaying user summary information in a card layout.
 * Used in UserSearchScreen to show search results.
 *
 * **Composition**:
 * - Card (Material3)
 * - UserAvatar (Atom)
 * - Display name Text (Atom)
 * - Nickname Text (Atom)
 * - Profile snippet Text (Atom)
 *
 * **Usage**:
 * ```kotlin
 * UserCard(
 *     user = connpassUser,
 *     onClick = { navigateToUserDetail(user) }
 * )
 * ```
 *
 * **Reusability**:
 * - UserSearchScreen: Search results list
 * - EventDetailScreen (future): Participant list
 * - Any screen displaying user summary cards
 *
 * @param user The ConnpassUser to display
 * @param onClick Callback when card is clicked
 * @param modifier Modifier for customization
 */
@Composable
fun UserCard(
    user: ConnpassUser,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar
            UserAvatar(size = 56.dp)

            // User Information
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Display Name
                Text(
                    text = user.getDisplayNameOrNickname(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Nickname (if different from display name)
                if (user.displayName != user.nickname) {
                    Text(
                        text = "@${user.nickname}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Profile snippet (if available)
                if (user.hasProfile()) {
                    Text(
                        text = user.description?.take(100) ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
