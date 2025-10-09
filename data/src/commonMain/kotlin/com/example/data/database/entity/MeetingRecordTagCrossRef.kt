package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Room cross-reference entity for the many-to-many relationship between
 * meeting records and tags.
 * Maps to the "meeting_record_tag_cross_ref" table.
 *
 * Implementation by: data-layer-architect (coordinated by project-orchestrator)
 * PBI-5, Task 1.15: MeetingRecordTagCrossRef for Meeting Notes & Tagging
 *
 * This entity enables multiple tags to be associated with a single meeting record,
 * and allows tags to be reused across multiple meeting records.
 *
 * Foreign key constraints with CASCADE delete ensure that:
 * - When a meeting record is deleted, its tag associations are automatically removed
 * - When a tag is deleted, its associations with meeting records are automatically removed
 *
 * The composite primary key (meetingRecordId, tagId) prevents duplicate associations.
 *
 * @property meetingRecordId Foreign key to meeting_records.id
 * @property tagId Foreign key to tags.id
 */
@Entity(
    tableName = "meeting_record_tag_cross_ref",
    primaryKeys = ["meetingRecordId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = MeetingRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["meetingRecordId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["meetingRecordId"]),
        Index(value = ["tagId"])
    ]
)
data class MeetingRecordTagCrossRef(
    val meetingRecordId: Long,  // FK to meeting_records.id
    val tagId: Long             // FK to tags.id
)
