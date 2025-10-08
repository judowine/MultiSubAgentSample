package org.example.project.judowine.ui.component.atom

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Atomic Design - ATOM: Loading Indicator
 *
 * Implemented by: project-orchestrator (Task 3.9)
 * PBI-1, Task 3.9: Atomic Design component extraction
 *
 * A simple loading indicator (CircularProgressIndicator) following Material3 design.
 *
 * **Usage**:
 * ```kotlin
 * // Centered on screen
 * LoadingIndicator(modifier = Modifier.fillMaxSize())
 *
 * // Small indicator in button
 * LoadingIndicator(size = 24.dp, color = MaterialTheme.colorScheme.onPrimary)
 * ```
 *
 * @param modifier Modifier for customization (use fillMaxSize() for full-screen centered)
 * @param size The size of the indicator (default: default Material3 size)
 * @param color The color of the indicator (default: primary color)
 * @param centered Whether to center the indicator in the available space (default: true)
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    centered: Boolean = true
) {
    val indicatorModifier = if (size != null) {
        Modifier.size(size)
    } else {
        Modifier
    }

    if (centered) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = indicatorModifier,
                color = color
            )
        }
    } else {
        CircularProgressIndicator(
            modifier = modifier.then(indicatorModifier),
            color = color
        )
    }
}
