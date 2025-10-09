package org.example.project.judowine.domain.mapper

import com.example.data.database.entity.MeetingRecordEntity
import org.example.project.judowine.domain.model.MeetingRecord

/**
 * Mapper functions to convert between MeetingRecordEntity (data layer) and MeetingRecord (domain model).
 *
 * Implementation by: tactical-ddd-shared-implementer (coordinated by project-orchestrator)
 * PBI-4, Task 7.2: MeetingRecordEntity ↔ MeetingRecord mapper
 *
 * Following the architecture rule: composeApp → shared → data
 * - /data module returns MeetingRecordEntity (has no knowledge of domain models)
 * - /shared module handles conversion via these mappers
 * - Use Cases in /shared orchestrate the mapping
 *
 * Key transformations:
 * - Both MeetingRecordEntity and MeetingRecord have the same field types
 * - Direct field-to-field mapping with no data type transformations needed
 * - All fields are non-nullable on both sides
 *
 * Note on userId type:
 * - MeetingRecordEntity uses Long (database storage type)
 * - MeetingRecord uses Long (domain model type)
 * - Direct mapping without conversion
 */

/**
 * Converts MeetingRecordEntity (data layer) to MeetingRecord domain model.
 *
 * Performs direct field-to-field mapping as both entities share the same structure.
 *
 * @receiver MeetingRecordEntity from the data layer
 * @return MeetingRecord domain model
 */
fun MeetingRecordEntity.toDomainModel(): MeetingRecord {
    return MeetingRecord(
        id = id,
        eventId = eventId,
        userId = userId,
        nickname = nickname,
        createdAt = createdAt
    )
}

/**
 * Converts a list of MeetingRecordEntity to a list of MeetingRecord domain models.
 *
 * @receiver List of MeetingRecordEntity from the data layer
 * @return List of MeetingRecord domain models
 */
fun List<MeetingRecordEntity>.toDomainModels(): List<MeetingRecord> {
    return map { it.toDomainModel() }
}

/**
 * Converts MeetingRecord domain model to MeetingRecordEntity (data layer).
 *
 * Performs direct field-to-field mapping for persistence.
 *
 * @receiver MeetingRecord domain model
 * @return MeetingRecordEntity for persistence
 */
fun MeetingRecord.toEntity(): MeetingRecordEntity {
    return MeetingRecordEntity(
        id = id,
        eventId = eventId,
        userId = userId,
        nickname = nickname,
        createdAt = createdAt
    )
}
