package org.example.project.judowine.ui.component.molecule

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

/**
 * Atomic Design - MOLECULE: Search Input Field
 *
 * Implemented by: compose-ui-architect
 * PBI-3, Tasks 5.6 & 6.5: Extract reusable UI components for user search and detail screens
 *
 * A molecule component combining a text field with search icon and clear button.
 * Used for search functionality across different screens (user search, event search, etc.).
 *
 * **Composition**:
 * - OutlinedTextField (Material3)
 * - Search icon (🔍 emoji, Atom)
 * - Clear button (✕ IconButton, Atom)
 * - Placeholder text (Atom)
 *
 * **Usage**:
 * ```kotlin
 * SearchInputField(
 *     query = searchQuery,
 *     onQueryChange = { query ->
 *         viewModel.handleIntent(SearchIntent.Search(query))
 *     },
 *     onClear = { viewModel.handleIntent(SearchIntent.ClearResults) },
 *     placeholder = "Search by nickname..."
 * )
 * ```
 *
 * **Reusability**:
 * - UserSearchScreen: Search users by nickname
 * - EventSearchScreen (future): Search events by title/keyword
 * - Any screen requiring search functionality
 *
 * @param query Current search query text
 * @param onQueryChange Callback when query changes
 * @param onClear Callback when clear button is clicked
 * @param placeholder Placeholder text to display when empty
 * @param modifier Modifier for customization
 */
@Composable
fun SearchInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingIcon = {
            Text(
                text = "🔍",
                style = MaterialTheme.typography.titleMedium
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Text(
                        text = "✕",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}
