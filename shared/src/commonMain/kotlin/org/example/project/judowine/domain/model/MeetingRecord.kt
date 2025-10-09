package org.example.project.judowine.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.hours

/**
 * Domain model representing a meeting record in the EventMeet application.
 *
 * This is a DDD Entity with identity defined by [id].
 * The domain model is independent of persistence concerns and uses pure Kotlin types.
 *
 * Implementation by: tactical-ddd-shared-implementer (coordinated by project-orchestrator)
 * PBI-4, Task 7.1: MeetingRecord domain model
 *
 * A MeetingRecord captures the fact that a user met someone at a specific event,
 * enabling networking tracking and social connection management within EventMeet.
 *
 * Business Rules:
 * - Each meeting record must have a unique database ID
 * - eventId must reference a valid event (positive value)
 * - userId must be a valid connpass user ID (positive value)
 * - nickname must not be blank (cached from API for display)
 * - createdAt timestamp tracks when the meeting was recorded
 *
 * @property id Unique identifier (Long) - defines entity identity, database primary key
 * @property eventId connpass event ID where the meeting occurred (must be positive)
 * @property userId connpass user ID of the person met (must be positive)
 * @property nickname User's display name (cached from API, must not be blank)
 * @property createdAt Timestamp when this meeting record was created
 *
 * @throws IllegalArgumentException if validation rules are violated
 */
data class MeetingRecord(
    val id: Long,
    val eventId: Long,
    val userId: Long,
    val nickname: String,
    val createdAt: Instant
) {
    init {
        // Domain invariants - enforce business rules at construction
        require(eventId > 0) { "Event ID must be positive" }
        require(userId > 0) { "User ID must be positive" }
        require(nickname.isNotBlank()) { "Nickname must not be blank" }
    }

    /**
     * Equality is based on identity (id), not state.
     * This follows DDD Entity pattern where entities with the same ID are considered equal,
     * even if their other properties differ.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MeetingRecord

        return id == other.id
    }

    /**
     * Hash code based on identity (id) to maintain consistency with equals.
     */
    override fun hashCode(): Int {
        return id.hashCode()
    }

    /**
     * Returns whether this meeting record was created within the last 24 hours.
     * Useful for highlighting recently added connections in the UI.
     *
     * @return true if created within last 24 hours, false otherwise
     */
    fun isRecentlyCreated(): Boolean {
        val now = Clock.System.now()
        val twentyFourHoursAgo = now - 24.hours
        return createdAt >= twentyFourHoursAgo
    }

    /**
     * Returns whether this meeting record is for the specified user.
     * Useful for filtering meeting records by specific attendee.
     *
     * @param userId The connpass user ID to check
     * @return true if this record matches the given userId, false otherwise
     */
    fun isSameAttendee(userId: Long): Boolean {
        return this.userId == userId
    }

    /**
     * Returns whether this meeting record belongs to the specified event.
     *
     * @param eventId The event ID to check
     * @return true if this record matches the given eventId, false otherwise
     */
    fun isSameEvent(eventId: Long): Boolean {
        return this.eventId == eventId
    }
}
