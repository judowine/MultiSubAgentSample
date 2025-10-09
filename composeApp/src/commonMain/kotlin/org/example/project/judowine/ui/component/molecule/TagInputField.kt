package org.example.project.judowine.ui.component.molecule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.judowine.domain.model.Tag

/**
 * Atomic Design - MOLECULE: Tag Input Field
 *
 * Implemented by: compose-ui-architect (coordinated by project-orchestrator)
 * PBI-5, Task 9.6: TagInputField molecule component
 *
 * A molecule component for inputting tags with autocomplete suggestions.
 * Used in EditMeetingRecordScreen for adding/editing meeting tags.
 *
 * **Composition**:
 * - TextField with autocomplete dropdown (Material3)
 * - DropdownMenu for tag suggestions
 * - "Add" button to create tag from current input
 * - FlowRow of selected tags as dismissible InputChips
 *
 * **Design Notes**:
 * - Stateless component - receives all data through parameters
 * - Autocomplete filters suggestions based on input text
 * - Allows creating new tags not in suggestions
 * - Selected tags displayed as dismissible chips
 * - Material3 design with InputChip for tag display
 * - Reusable across any tagging functionality
 *
 * **Usage**:
 * ```kotlin
 * TagInputField(
 *     selectedTags = selectedTagsList,
 *     availableTags = allTagsList,
 *     onTagAdded = { tagName -> selectedTagsList.add(tagName) },
 *     onTagRemoved = { tagName -> selectedTagsList.remove(tagName) }
 * )
 * ```
 *
 * @param selectedTags List of currently selected tag names
 * @param availableTags List of available Tag domain models for autocomplete
 * @param onTagAdded Callback when a tag is added (via suggestion or manual input)
 * @param onTagRemoved Callback when a tag is removed (via chip dismiss)
 * @param modifier Modifier for customization
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TagInputField(
    selectedTags: List<String>,
    availableTags: List<Tag>,
    onTagAdded: (String) -> Unit,
    onTagRemoved: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var tagInputText by remember { mutableStateOf("") }
    var showDropdown by remember { mutableStateOf(false) }

    // Filter suggestions based on input text and exclude already selected tags
    val filteredSuggestions = remember(tagInputText, availableTags, selectedTags) {
        if (tagInputText.isBlank()) {
            emptyList()
        } else {
            availableTags
                .filter { tag ->
                    tag.name.contains(tagInputText, ignoreCase = true) &&
                            !selectedTags.contains(tag.name)
                }
                .take(5) // Limit to 5 suggestions
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tag input field with autocomplete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = tagInputText,
                    onValueChange = { newValue ->
                        tagInputText = newValue
                        showDropdown = newValue.isNotBlank() && filteredSuggestions.isNotEmpty()
                    },
                    label = {
                        Text(
                            text = "Add Tag",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    placeholder = {
                        Text(
                            text = "Type tag name...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = MaterialTheme.shapes.medium
                )

                // Autocomplete dropdown
                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    filteredSuggestions.forEach { tag ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = tag.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                onTagAdded(tag.name)
                                tagInputText = ""
                                showDropdown = false
                            }
                        )
                    }
                }
            }

            // Add button
            Button(
                onClick = {
                    if (tagInputText.isNotBlank() && !selectedTags.contains(tagInputText)) {
                        onTagAdded(tagInputText.trim())
                        tagInputText = ""
                        showDropdown = false
                    }
                },
                enabled = tagInputText.isNotBlank() && !selectedTags.contains(tagInputText.trim())
            ) {
                Text(
                    text = "Add",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Selected tags display (dismissible chips)
        if (selectedTags.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Selected Tags:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedTags.forEach { tagName ->
                        InputChip(
                            selected = true,
                            onClick = { /* Chips are dismissible, not clickable */ },
                            label = {
                                Text(
                                    text = tagName,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            trailingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { onTagRemoved(tagName) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "×",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
