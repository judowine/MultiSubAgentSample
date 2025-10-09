package org.example.project.judowine.ui.component.atom

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Atomic Design - ATOM: User Avatar
 *
 * Implemented by: compose-ui-architect
 * PBI-3, Tasks 5.6 & 6.5: Extract reusable UI components for user search and detail screens
 *
 * A basic atom component displaying a circular user avatar with emoji placeholder.
 * This can be extended to support image URLs using AsyncImage in the future.
 *
 * **Composition**:
 * - Circular Surface (Material3)
 * - Emoji placeholder text (👤)
 *
 * **Usage**:
 * ```kotlin
 * // Small avatar (search results)
 * UserAvatar(size = 56.dp)
 *
 * // Large avatar (profile header)
 * UserAvatar(size = 120.dp)
 * ```
 *
 * **Future Enhancement**:
 * - Add `imageUrl: String?` parameter to load actual user images
 * - Use Coil/AsyncImage for cross-platform image loading
 * - Add fallback to emoji if image fails to load
 *
 * @param size The diameter of the circular avatar
 * @param modifier Modifier for customization
 */
@Composable
fun UserAvatar(
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "👤",
                style = when {
                    size >= 120.dp -> MaterialTheme.typography.displayLarge
                    size >= 80.dp -> MaterialTheme.typography.displayMedium
                    size >= 56.dp -> MaterialTheme.typography.headlineMedium
                    else -> MaterialTheme.typography.titleMedium
                }
            )
        }
    }
}
