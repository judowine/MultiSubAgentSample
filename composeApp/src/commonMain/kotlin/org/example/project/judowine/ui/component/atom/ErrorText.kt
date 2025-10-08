package org.example.project.judowine.ui.component.atom

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign

/**
 * Atomic Design - ATOM: Error Text
 *
 * Implemented by: project-orchestrator (Task 3.9)
 * PBI-1, Task 3.9: Atomic Design component extraction
 *
 * A text component styled for error messages following Material3 design.
 *
 * **Usage**:
 * ```kotlin
 * ErrorText(text = "Invalid input")
 * ErrorText(text = "Network error", textAlign = TextAlign.Center)
 * ```
 *
 * @param text The error message to display
 * @param modifier Modifier for customization
 * @param textAlign Text alignment (default: start)
 * @param style Text style (default: bodySmall)
 * @param color Text color (default: error color from theme)
 */
@Composable
fun ErrorText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    color: Color = MaterialTheme.colorScheme.error
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        textAlign = textAlign
    )
}
