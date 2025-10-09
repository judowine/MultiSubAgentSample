package org.example.project.judowine.domain.mapper

import com.example.data.database.entity.MeetingRecordEntity
import com.example.data.database.entity.MeetingRecordWithTags
import org.example.project.judowine.domain.model.MeetingRecord

/**
 * Mapper functions to convert between MeetingRecordEntity (data layer) and MeetingRecord (domain model).
 *
 * Implementation by: tactical-ddd-shared-implementer (coordinated by project-orchestrator)
 * PBI-4, Task 7.2: MeetingRecordEntity ↔ MeetingRecord mapper
 * PBI-5, Task 8.3: Added MeetingRecordWithTags mapping
 *
 * Following the architecture rule: composeApp → shared → data
 * - /data module returns MeetingRecordEntity (has no knowledge of domain models)
 * - /shared module handles conversion via these mappers
 * - Use Cases in /shared orchestrate the mapping
 *
 * Key transformations:
 * - Both MeetingRecordEntity and MeetingRecord have the same field types
 * - Direct field-to-field mapping with no data type transformations needed
 * - MeetingRecordWithTags includes associated tags (many-to-many relationship)
 * - Tag names are extracted from TagEntity list and mapped to domain model
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
 * Note: This function does not include tags. For meeting records with tags,
 * use MeetingRecordWithTags.toDomainModel() instead.
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
        notes = notes,
        tags = emptyList(), // No tags available in plain entity
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
 * Note: This function does not persist tags. Tags are managed separately
 * via the MeetingRecordTagCrossRef junction table in the repository layer.
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
        notes = notes,
        createdAt = createdAt
    )
}

/**
 * Converts MeetingRecordWithTags (data layer) to MeetingRecord domain model.
 *
 * This function extracts tag names from the TagEntity list and includes them
 * in the domain model along with the meeting record fields and notes.
 *
 * PBI-5, Task 8.3: MeetingRecordWithTags to domain model mapping
 *
 * @receiver MeetingRecordWithTags from the data layer
 * @return MeetingRecord domain model with notes and tag names
 */
fun MeetingRecordWithTags.toDomainModel(): MeetingRecord {
    return MeetingRecord(
        id = meetingRecord.id,
        eventId = meetingRecord.eventId,
        userId = meetingRecord.userId,
        nickname = meetingRecord.nickname,
        notes = meetingRecord.notes,
        tags = tags.map { it.name },
        createdAt = meetingRecord.createdAt
    )
}

/**
 * Converts a list of MeetingRecordWithTags to a list of MeetingRecord domain models.
 *
 * @receiver List of MeetingRecordWithTags from the data layer
 * @return List of MeetingRecord domain models with notes and tags
 */
fun List<MeetingRecordWithTags>.toDomainModelsWithTags(): List<MeetingRecord> {
    return map { it.toDomainModel() }
}
