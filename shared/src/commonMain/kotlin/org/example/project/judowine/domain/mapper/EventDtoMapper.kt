package org.example.project.judowine.domain.mapper

import com.example.data.network.dto.EventDto
import kotlinx.datetime.Instant
import org.example.project.judowine.domain.model.Event

/**
 * Mapper functions to convert EventDto (API response) to Event domain model.
 *
 * Implementation by: tactical-ddd-shared-implementer
 * PBI-2, Task 4.2: EventDto → Event mapper
 *
 * Following the architecture rule: composeApp → shared → data
 * - /data module returns EventDto (has no knowledge of domain models)
 * - /shared module handles conversion via these mappers
 * - Use Cases in /shared orchestrate the mapping
 *
 * Key transformations:
 * - String datetime fields → Instant (kotlinx-datetime)
 * - eventUrl → url (field name mapping)
 * - Empty/null descriptions → null (nullable domain field)
 * - Empty/null addresses → null (nullable domain field)
 * - limit: null or 0 → null (unlimited capacity)
 * - Temporary id = 0 (will be assigned by Room on insert)
 */

/**
 * Converts EventDto (API response) to Event domain model.
 *
 * Parses ISO 8601 datetime strings from the API into kotlinx.datetime.Instant.
 * Handles nullable fields and converts API conventions to domain model conventions.
 *
 * @receiver EventDto from the connpass API
 * @return Event domain model
 * @throws IllegalArgumentException if datetime parsing fails or domain invariants are violated
 */
fun EventDto.toDomainModel(): Event {
    return Event(
        eventId = eventId,
        title = title,
        description = description.takeIf { it.isNotBlank() },
        startedAt = Instant.parse(startedAt),
        endedAt = Instant.parse(endedAt),
        url = eventUrl,
        address = address?.takeIf { it.isNotBlank() },
        limit = when {
            limit == null || limit == 0 -> null // null or 0 = unlimited
            else -> limit
        },
        accepted = accepted,
        waiting = waiting
    )
}

/**
 * Converts a list of EventDto to a list of Event domain models.
 *
 * @receiver List of EventDto from the connpass API
 * @return List of Event domain models
 */
fun List<EventDto>.toDomainModels(): List<Event> {
    return map { it.toDomainModel() }
}
