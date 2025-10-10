package org.example.project.judowine.domain.model

/**
 * Domain model representing a connpass user discovered through search.
 *
 * IMPORTANT: This is DISTINCT from User (profile model from PBI-1):
 * - User = logged-in user's own profile (persisted in Room database)
 * - ConnpassUser = OTHER users discovered through search (API-only, no persistence)
 *
 * This model exhibits Value Object-like characteristics:
 * - Primarily used for read-only display and comparison operations
 * - Immutable data structure with domain logic methods
 * - Equality based on userId (unique identifier from connpass API)
 *
 * Implementation by: tactical-ddd-shared-implementer
 * PBI-3, Task 5.1: ConnpassUser domain model for user search feature
 * Fixed: Field names updated to match actual API response
 *
 * @property userId connpass user ID (unique identifier from API)
 * @property nickname User's login name/nickname on connpass
 * @property displayName User's public display name
 * @property description User bio/profile description (nullable, may contain HTML)
 * @property imageUrl Profile image URL (nullable)
 * @property url User's connpass profile page URL
 *
 * @throws IllegalArgumentException if validation rules are violated
 */
data class ConnpassUser(
    val userId: Int,
    val nickname: String,
    val displayName: String,
    val description: String?,
    val imageUrl: String?,
    val url: String
) {
    init {
        // Domain invariants - enforce business rules at construction
        require(userId > 0) { "User ID must be positive" }
        require(nickname.isNotBlank()) { "Nickname must not be blank" }
        require(displayName.isNotBlank()) { "Display name must not be blank" }
        require(url.isNotBlank()) { "Connpass URL must not be blank" }
    }

    /**
     * Returns whether the user has a non-empty profile description.
     *
     * Domain logic: A user is considered to have a profile if the description field
     * is not null and contains meaningful content (not just whitespace).
     *
     * @return true if description is present and non-blank, false otherwise
     */
    fun hasProfile(): Boolean {
        return !description.isNullOrBlank()
    }

    /**
     * Returns the most appropriate display name with fallback logic.
     *
     * Domain logic: Prefer displayName, but if it's empty or identical to nickname,
     * fall back to nickname. This ensures we always show a meaningful name to users.
     *
     * @return displayName if meaningful, otherwise nickname
     */
    fun getDisplayNameOrNickname(): String {
        return when {
            displayName.isBlank() -> nickname
            displayName == nickname -> nickname
            else -> displayName
        }
    }

    /**
     * Returns whether the user has a custom profile image.
     *
     * Domain logic: A user is considered to have a custom icon if imageUrl is not null.
     *
     * @return true if imageUrl is present, false otherwise (using default icon)
     */
    fun hasCustomIcon(): Boolean {
        return !imageUrl.isNullOrBlank()
    }
}
