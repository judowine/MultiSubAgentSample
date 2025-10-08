package org.example.project.judowine.domain.model

import kotlinx.datetime.Instant

/**
 * Domain model representing a connpass event in the EventMeet application.
 *
 * This is a DDD Entity with identity defined by [id].
 * The domain model is independent of persistence concerns and uses pure Kotlin types.
 *
 * Implementation by: tactical-ddd-shared-implementer
 * PBI-2, Task 4.1: Event domain model aligned with database layer
 *
 * @property id Unique identifier (Long) - defines entity identity, matches database primary key
 * @property eventId External identifier from Connpass platform (connpass event ID)
 * @property title Event title/name (must be non-empty)
 * @property description Event description (may contain HTML, nullable for events without description)
 * @property startedAt Timestamp when the event starts
 * @property endedAt Timestamp when the event ends (nullable for events without end time)
 * @property url Event page URL on connpass (must be non-empty)
 * @property address Event venue address (nullable for online-only events)
 * @property limit Maximum number of participants (nullable for unlimited events, must be >= 0 when present)
 * @property accepted Number of accepted participants (must be >= 0)
 * @property waiting Number of participants on waiting list (must be >= 0)
 *
 * @throws IllegalArgumentException if validation rules are violated
 */
data class Event(
    val id: Long,
    val eventId: Long,
    val title: String,
    val description: String?,
    val startedAt: Instant,
    val endedAt: Instant?,
    val url: String,
    val address: String?,
    val limit: Int?,
    val accepted: Int,
    val waiting: Int
) {
    init {
        // Domain invariants - enforce business rules at construction
        require(title.isNotBlank()) { "Event title must not be blank" }
        require(url.isNotBlank()) { "Event URL must not be blank" }
        require(eventId > 0) { "Event ID must be positive" }
        require(accepted >= 0) { "Accepted count must not be negative" }
        require(waiting >= 0) { "Waiting count must not be negative" }

        // Validate limit if present
        limit?.let {
            require(it >= 0) { "Participant limit must not be negative" }
        }

        // Validate time order if endedAt is present
        endedAt?.let {
            require(it >= startedAt) { "Event end time must not be before start time" }
        }
    }

    /**
     * Equality is based on identity (id), not state.
     * This follows DDD Entity pattern where entities with the same ID are considered equal,
     * even if their other properties differ.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Event

        return id == other.id
    }

    /**
     * Hash code based on identity (id) to maintain consistency with equals.
     */
    override fun hashCode(): Int {
        return id.hashCode()
    }

    /**
     * Returns whether the event has reached its participant limit.
     * An event without a limit (null) is considered to have unlimited capacity.
     *
     * @return true if limit is set and accepted count has reached or exceeded it, false otherwise
     */
    fun isFull(): Boolean {
        return limit?.let { accepted >= it } ?: false
    }

    /**
     * Returns whether the event has a waiting list with participants.
     *
     * @return true if waiting count is greater than 0, false otherwise
     */
    fun hasWaitingList(): Boolean {
        return waiting > 0
    }

    /**
     * Returns whether the event has an unlimited participant capacity.
     *
     * @return true if limit is null or 0, false otherwise
     */
    fun isUnlimited(): Boolean {
        return limit == null || limit == 0
    }

    /**
     * Returns the number of available slots for this event.
     * Returns null if the event has unlimited capacity.
     *
     * @return Available slots (limit - accepted), or null if unlimited
     */
    fun availableSlots(): Int? {
        return when {
            isUnlimited() -> null
            else -> maxOf(0, limit!! - accepted) // Use maxOf to prevent negative values
        }
    }

    /**
     * Returns whether the event is an online event (no physical venue).
     *
     * @return true if address is null or blank, false otherwise
     */
    fun isOnlineEvent(): Boolean {
        return address.isNullOrBlank()
    }

    /**
     * Returns the total number of participants (accepted + waiting).
     *
     * @return Sum of accepted and waiting participants
     */
    fun totalParticipants(): Int {
        return accepted + waiting
    }
}
