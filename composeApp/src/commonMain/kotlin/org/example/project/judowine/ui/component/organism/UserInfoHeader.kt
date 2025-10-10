package org.example.project.judowine.ui.component.organism

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.project.judowine.domain.model.ConnpassUser
import org.example.project.judowine.ui.component.atom.UserAvatar
import org.example.project.judowine.ui.component.molecule.SocialLinkButton

/**
 * Atomic Design - ORGANISM: User Info Header
 *
 * Implemented by: compose-ui-architect
 * PBI-3, Tasks 5.6 & 6.5: Extract reusable UI components for user search and detail screens
 * Fixed: Removed Twitter/GitHub links (not in actual API response)
 *
 * An organism component displaying comprehensive user profile information.
 * Combines UserAvatar, text atoms, and SocialLinkButton molecules into a complete profile header.
 *
 * **Composition**:
 * - UserAvatar (Atom) - Large 120dp avatar
 * - Display name Text (Atom)
 * - Nickname Text (Atom)
 * - Bio Card (Molecule)
 * - Social links section with SocialLinkButton molecules:
 *   - Connpass profile link (always present)
 *
 * **Usage**:
 * ```kotlin
 * UserInfoHeader(
 *     user = connpassUser,
 *     onConnpassClick = { url -> openUrl(url) }
 * )
 * ```
 *
 * **Reusability**:
 * - UserDetailScreen: Full user profile display
 * - ProfileScreen (future): Logged-in user's own profile
 * - Any screen requiring complete user information display
 *
 * @param user The ConnpassUser to display
 * @param onConnpassClick Callback when Connpass link is clicked (receives URL)
 * @param modifier Modifier for customization
 */
@Composable
fun UserInfoHeader(
    user: ConnpassUser,
    onConnpassClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User Avatar (large, centered)
        UserAvatar(size = 120.dp)

        Spacer(modifier = Modifier.height(8.dp))

        // Display Name
        Text(
            text = user.getDisplayNameOrNickname(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // Nickname (if different from display name)
        if (user.displayName != user.nickname) {
            Text(
                text = "@${user.nickname}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Bio (if available)
        if (user.hasProfile()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = user.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Social Links Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Connpass Profile Link (always available)
            SocialLinkButton(
                label = "View on Connpass",
                onClick = { onConnpassClick(user.url) },
                isPrimary = true
            )
        }
    }
}
