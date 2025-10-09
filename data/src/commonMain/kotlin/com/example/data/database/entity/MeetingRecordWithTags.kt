package com.example.data.database.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Room relation class representing a meeting record with its associated tags.
 *
 * Implementation by: data-layer-architect (coordinated by project-orchestrator)
 * PBI-5, Task 1.17: MeetingRecordWithTags for Meeting Notes & Tagging
 *
 * This class is used by Room to automatically query and populate the many-to-many
 * relationship between meeting records and tags via the MeetingRecordTagCrossRef junction table.
 *
 * Room will automatically:
 * 1. Query the meeting_records table for the MeetingRecordEntity
 * 2. Use the junction table to find associated tag IDs
 * 3. Query the tags table to populate the tags list
 *
 * This is returned by repository queries to provide complete meeting record data
 * including notes and tags for domain layer conversion.
 *
 * @property meetingRecord The embedded meeting record entity
 * @property tags List of associated tags (fetched via junction table)
 */
data class MeetingRecordWithTags(
    @Embedded
    val meetingRecord: MeetingRecordEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = MeetingRecordTagCrossRef::class,
            parentColumn = "meetingRecordId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity> = emptyList()
)
