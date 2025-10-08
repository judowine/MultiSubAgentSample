package org.example.project.judowine.domain.mapper

import com.example.data.database.entity.EventEntity
import org.example.project.judowine.domain.model.Event

/**
 * Mapper functions to convert between EventEntity (data layer) and Event (domain model).
 *
 * Implementation by: tactical-ddd-shared-implementer
 * PBI-2, Task 4.3: EventEntity ↔ Event mapper
 *
 * Following the architecture rule: composeApp → shared → data
 * - /data module returns EventEntity (has no knowledge of domain models)
 * - /shared module handles conversion via these mappers
 * - Use Cases in /shared orchestrate the mapping
 *
 * Key transformations:
 * - EventEntity has non-nullable fields where Event has nullable
 * - Entity limit: 0 → Domain limit: null (unlimited capacity)
 * - Entity address/endedAt: empty/default → Domain: null
 * - Domain nullable fields → Entity: default values (empty string, 0, or epoch time)
 */

/**
 * Converts EventEntity (data layer) to Event domain model.
 *
 * Handles conversion of database-friendly representations to domain-friendly nullable types.
 *
 * @receiver EventEntity from the data layer
 * @return Event domain model
 */
fun EventEntity.toDomainModel(): Event {
    return Event(
        id = id,
        eventId = eventId,
        title = title,
        description = description.takeIf { it.isNotBlank() },
        startedAt = startedAt,
        endedAt = endedAt.takeIf { it != startedAt }, // If equal, treat as no end time
        url = url,
        address = address.takeIf { it.isNotBlank() },
        limit = when (limit) {
            0 -> null // 0 in entity = unlimited capacity
            else -> limit
        },
        accepted = accepted,
        waiting = waiting
    )
}

/**
 * Converts a list of EventEntity to a list of Event domain models.
 *
 * @receiver List of EventEntity from the data layer
 * @return List of Event domain models
 */
fun List<EventEntity>.toDomainModels(): List<Event> {
    return map { it.toDomainModel() }
}

/**
 * Converts Event domain model to EventEntity (data layer).
 *
 * Handles conversion of nullable domain fields to non-nullable database fields
 * using appropriate default values.
 *
 * @receiver Event domain model
 * @return EventEntity for persistence
 */
fun Event.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        eventId = eventId,
        title = title,
        description = description ?: "", // Empty string for null descriptions
        startedAt = startedAt,
        endedAt = endedAt ?: startedAt, // Use startedAt as default if no end time
        url = url,
        address = address ?: "", // Empty string for null addresses (online events)
        limit = limit ?: 0, // 0 represents unlimited capacity
        accepted = accepted,
        waiting = waiting
    )
}
