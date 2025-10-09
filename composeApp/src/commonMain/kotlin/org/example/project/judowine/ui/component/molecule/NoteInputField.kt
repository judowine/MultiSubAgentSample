package org.example.project.judowine.ui.component.molecule

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Atomic Design - MOLECULE: Note Input Field
 *
 * Implemented by: compose-ui-architect (coordinated by project-orchestrator)
 * PBI-5, Task 9.7: NoteInputField molecule component
 *
 * A molecule component for inputting multiline notes with character counter.
 * Used in EditMeetingRecordScreen for adding/editing meeting notes.
 *
 * **Composition**:
 * - OutlinedTextField (Material3) - multiline text input
 * - Character counter label showing current/max length
 *
 * **Design Notes**:
 * - Stateless component - receives all data through parameters
 * - Expandable multiline TextField (up to 5 lines visible, scrollable beyond)
 * - Character counter prevents exceeding max length
 * - Material3 design with proper validation and error states
 * - Reusable across any note-taking features
 *
 * **Usage**:
 * ```kotlin
 * NoteInputField(
 *     notes = noteText,
 *     onNotesChange = { newNotes -> noteText = newNotes },
 *     maxLength = 500
 * )
 * ```
 *
 * @param notes The current notes text
 * @param onNotesChange Callback when notes text changes
 * @param maxLength Maximum character length (default 500)
 * @param modifier Modifier for customization
 */
@Composable
fun NoteInputField(
    notes: String,
    onNotesChange: (String) -> Unit,
    maxLength: Int = 500,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Multiline text field for notes
        OutlinedTextField(
            value = notes,
            onValueChange = { newValue ->
                // Enforce character limit
                if (newValue.length <= maxLength) {
                    onNotesChange(newValue)
                }
            },
            label = {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            placeholder = {
                Text(
                    text = "Add notes about this meeting...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge,
            minLines = 3,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = MaterialTheme.shapes.medium
        )

        // Character counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${notes.length} / $maxLength",
                style = MaterialTheme.typography.bodySmall,
                color = if (notes.length >= maxLength) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
