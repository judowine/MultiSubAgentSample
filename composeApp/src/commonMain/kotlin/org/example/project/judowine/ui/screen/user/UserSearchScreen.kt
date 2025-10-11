package org.example.project.judowine.ui.screen.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.project.judowine.domain.model.ConnpassUser
import org.example.project.judowine.ui.component.molecule.SearchInputField
import org.example.project.judowine.ui.component.molecule.UserCard
import org.example.project.judowine.ui.component.organism.EmptyUserSearchState

/**
 * User Search Screen for EventMeet application.
 *
 * Implemented by: compose-ui-architect
 * PBI-3, Task 5.4: UserSearchScreen for user search & discovery
 * Updated: Changed from real-time search to button-triggered search to avoid API rate limits
 *
 * This screen enables users to search for other connpass users by nickname with:
 * - Search input field with clear button
 * - Search button (triggers API call only when clicked)
 * - Search results list (scrollable)
 * - Empty state ("No users found")
 * - Loading state (searching...)
 * - Error state with retry
 * - Navigation to UserDetailScreen on user tap
 *
 * Design Notes:
 * - Follows Screen/Content separation pattern from PBI-1 and PBI-2
 * - Integrates with UserSearchViewModel using MVI pattern
 * - Button-triggered search prevents excessive API calls and rate limiting
 * - Stateless components for easy testing and reusability
 * - Material3 design system
 * - NO direct data layer access (uses ViewModel only)
 *
 * @param viewModel The UserSearchViewModel managing state
 * @param onUserClick Callback when a user is clicked (navigates to detail screen)
 * @param modifier Optional modifier for customization
 */
@Composable
fun UserSearchScreen(
    viewModel: UserSearchViewModel,
    onUserClick: (ConnpassUser) -> Unit,
    modifier: Modifier = Modifier
) {
    // Observe UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Track search query for TextField
    var searchQuery by remember { mutableStateOf("") }

    UserSearchContent(
        state = uiState,
        searchQuery = searchQuery,
        onSearchQueryChange = { query ->
            searchQuery = query
        },
        onSearchClick = {
            if (searchQuery.isNotBlank()) {
                viewModel.handleIntent(UserSearchIntent.Search(searchQuery))
            }
        },
        onClearSearch = {
            searchQuery = ""
            viewModel.handleIntent(UserSearchIntent.ClearResults)
        },
        onUserClick = onUserClick,
        onRetryClick = {
            // Re-trigger search with current query
            viewModel.handleIntent(UserSearchIntent.Search(searchQuery))
        },
        modifier = modifier
    )
}

/**
 * Stateless content composable for User Search.
 *
 * This component is designed to be reusable and testable. It receives all state
 * as parameters and emits events through callbacks (stateless pattern).
 *
 * @param state The current search state (Idle, Loading, Success, Empty, or Error)
 * @param searchQuery Current search query text
 * @param onSearchQueryChange Callback when search query changes (does not trigger search)
 * @param onSearchClick Callback when search button is clicked (triggers search)
 * @param onClearSearch Callback when clear button is clicked
 * @param onUserClick Callback when a user is clicked
 * @param onRetryClick Callback when retry button is clicked (in error state)
 * @param modifier Optional modifier for customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSearchContent(
    state: UserSearchUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onClearSearch: () -> Unit,
    onUserClick: (ConnpassUser) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Search Users",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input Field (extracted molecule component)
            SearchInputField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onClear = onClearSearch,
                placeholder = "Search by nickname...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            // Search Button
            Button(
                onClick = onSearchClick,
                enabled = searchQuery.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .height(56.dp)
            ) {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // State-based Content
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                when (state) {
                    is UserSearchUiState.Idle -> {
                        // Use extracted EmptyUserSearchState organism
                        EmptyUserSearchState(
                            icon = "🔍",
                            title = "Search for connpass users",
                            description = "Enter a nickname to find users"
                        )
                    }
                    is UserSearchUiState.Loading -> {
                        LoadingContent()
                    }
                    is UserSearchUiState.Success -> {
                        UserSearchResultsContent(
                            users = state.users,
                            onUserClick = onUserClick
                        )
                    }
                    is UserSearchUiState.Empty -> {
                        // Use extracted EmptyUserSearchState organism
                        EmptyUserSearchState()
                    }
                    is UserSearchUiState.Error -> {
                        ErrorContent(
                            message = state.message,
                            onRetryClick = onRetryClick
                        )
                    }
                }
            }
        }
    }
}

// SearchInputField, IdleContent, and EmptyContent have been extracted to reusable components:
// - SearchInputField -> ui.component.molecule.SearchInputField
// - IdleContent & EmptyContent -> ui.component.organism.EmptyUserSearchState

/**
 * Loading state content - displays a centered progress indicator.
 */
@Composable
private fun LoadingContent(
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
            CircularProgressIndicator()

            Text(
                text = "Searching...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Success state content - displays a scrollable list of search results.
 *
 * Each user card shows:
 * - User icon (if available, else placeholder)
 * - Display name (primary text)
 * - Nickname (secondary text)
 * - Profile snippet (if available)
 *
 * @param users List of ConnpassUser to display
 * @param onUserClick Callback when a user card is clicked
 */
@Composable
private fun UserSearchResultsContent(
    users: List<ConnpassUser>,
    onUserClick: (ConnpassUser) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Results header
        item {
            Text(
                text = "${users.size} user${if (users.size != 1) "s" else ""} found",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // User list (using extracted UserCard molecule component)
        items(
            items = users,
            key = { user -> user.userId }
        ) { user ->
            UserCard(
                user = user,
                onClick = { onUserClick(user) }
            )
        }
    }
}

// UserSearchResultCard has been extracted to reusable component:
// - UserSearchResultCard -> ui.component.molecule.UserCard

/**
 * Error state content - displays error message with retry button.
 *
 * @param message The error message to display
 * @param onRetryClick Callback when retry button is clicked
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .safeContentPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Search failed",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetryClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp)
        ) {
            Text(
                text = "Retry",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
