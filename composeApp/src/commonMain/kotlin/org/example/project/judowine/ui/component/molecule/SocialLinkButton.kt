package org.example.project.judowine.ui.component.molecule

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Atomic Design - MOLECULE: Social Link Button
 *
 * Implemented by: compose-ui-architect
 * PBI-3, Tasks 5.6 & 6.5: Extract reusable UI components for user search and detail screens
 *
 * A molecule component displaying a clickable social media or external profile link.
 * Supports primary (filled) and secondary (outlined) visual styles.
 *
 * **Composition**:
 * - Button or OutlinedButton (Material3)
 * - Icon emoji + Label text (Atoms)
 *
 * **Usage**:
 * ```kotlin
 * // Primary style (filled button)
 * SocialLinkButton(
 *     icon = "🐦",
 *     label = "Twitter: @username",
 *     onClick = { openUrl("https://twitter.com/username") },
 *     isPrimary = false
 * )
 *
 * // Primary style (Connpass profile)
 * SocialLinkButton(
 *     label = "View on Connpass",
 *     onClick = { openUrl(user.connpassUrl) },
 *     isPrimary = true
 * )
 * ```
 *
 * **Reusability**:
 * - UserDetailScreen: Twitter, GitHub, Connpass links
 * - Any screen displaying external profile links
 * - Social sharing features (future)
 *
 * @param label The button label text (e.g., "Twitter: @username")
 * @param onClick Callback when button is clicked
 * @param icon Optional icon emoji to display before label (e.g., "🐦", "🐙")
 * @param isPrimary If true, uses filled button style; if false, uses outlined style
 * @param modifier Modifier for customization
 */
@Composable
fun SocialLinkButton(
    label: String,
    onClick: () -> Unit,
    icon: String? = null,
    isPrimary: Boolean = false,
    modifier: Modifier = Modifier
) {
    val buttonContent: @Composable () -> Unit = {
        Text(
            text = if (icon != null) "$icon $label" else label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    if (isPrimary) {
        Button(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            buttonContent()
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            buttonContent()
        }
    }
}
