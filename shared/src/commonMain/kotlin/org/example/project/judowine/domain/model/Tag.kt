package org.example.project.judowine.domain.model

import kotlinx.datetime.Instant

/**
 * Domain model representing a tag in the EventMeet application.
 *
 * This is a DDD Value Object with equality based on [name], not [id].
 * The domain model is independent of persistence concerns and uses pure Kotlin types.
 *
 * Implementation by: tactical-ddd-shared-implementer (coordinated by project-orchestrator)
 * PBI-5, Task 8.2: Tag domain model as Value Object
 *
 * A Tag is a reusable label that can be applied to multiple meeting records,
 * enabling categorization and filtering of meetings (e.g., "client", "developer", "recruiter").
 *
 * Value Object Characteristics:
 * - Equality is based on name (two tags with same name are equal)
 * - Immutable (all properties are val)
 * - Constructor validation ensures name is not blank
 *
 * Business Rules:
 * - Tag name must not be blank
 * - Tags are compared by name, not by database ID
 * - Tag names are case-sensitive (per database schema)
 * - createdAt timestamp tracks when the tag was first created
 *
 * @property id Unique identifier (Long) - database primary key, not used for equality
 * @property name Tag name (unique, not blank) - defines value object equality
 * @property createdAt Timestamp when this tag was first created
 *
 * @throws IllegalArgumentException if name is blank
 */
data class Tag(
    val id: Long,
    val name: String,
    val createdAt: Instant
) {
    init {
        // Domain invariants - enforce business rules at construction
        require(name.isNotBlank()) { "Tag name must not be blank" }
    }

    /**
     * Equality is based on name, not id.
     * This follows DDD Value Object pattern where value objects with the same
     * name are considered equal, regardless of their database IDs.
     *
     * Two tags are equal if they have the same name (case-sensitive).
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Tag

        return name == other.name
    }

    /**
     * Hash code based on name to maintain consistency with equals.
     */
    override fun hashCode(): Int {
        return name.hashCode()
    }
}
