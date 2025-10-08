package org.example.project.judowine.domain.model

import kotlinx.datetime.Instant

/**
 * Domain model representing a user in the EventMeet application.
 *
 * This is a DDD Entity with identity defined by [id].
 * The domain model is independent of persistence concerns and uses pure Kotlin types.
 *
 * Implementation by: tactical-ddd-shared-implementer
 * PBI-1, Task 3.1: User domain model aligned with database layer
 *
 * @property id Unique identifier (Long) - defines entity identity, matches database primary key
 * @property connpassId External identifier from Connpass platform (must be non-empty)
 * @property nickname User's display name (must be non-empty)
 * @property createdAt Timestamp when the user profile was created
 * @property updatedAt Timestamp when the user profile was last modified
 *
 * @throws IllegalArgumentException if validation rules are violated
 */
data class User(
    val id: Long,
    val connpassId: String,
    val nickname: String,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    init {
        // Domain invariants - enforce business rules at construction
        require(connpassId.isNotBlank()) { "Connpass ID must not be blank" }
        require(nickname.isNotBlank()) { "Nickname must not be blank" }
        require(updatedAt >= createdAt) { "Updated timestamp must not be before created timestamp" }
    }

    /**
     * Equality is based on identity (id), not state.
     * This follows DDD Entity pattern where entities with the same ID are considered equal,
     * even if their other properties differ.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as User

        return id == other.id
    }

    /**
     * Hash code based on identity (id) to maintain consistency with equals.
     */
    override fun hashCode(): Int {
        return id.hashCode()
    }

    /**
     * Updates the user's nickname, returning a new instance with updated timestamp.
     *
     * This is a domain operation that encapsulates the business rule:
     * "When a user's nickname changes, the updatedAt timestamp must be updated."
     *
     * @param newNickname The new nickname to set (must be non-blank)
     * @param updatedAt The timestamp of this update
     * @return A new User instance with the updated nickname and timestamp
     * @throws IllegalArgumentException if newNickname is blank or updatedAt is before current updatedAt
     */
    fun updateNickname(newNickname: String, updatedAt: Instant): User {
        require(newNickname.isNotBlank()) { "New nickname must not be blank" }
        require(updatedAt >= this.updatedAt) { "New updated timestamp must not be before current timestamp" }

        return copy(
            nickname = newNickname,
            updatedAt = updatedAt
        )
    }

    /**
     * Returns whether this user profile has been modified since creation.
     *
     * @return true if updatedAt is different from createdAt, false otherwise
     */
    fun isModified(): Boolean = updatedAt != createdAt
}
