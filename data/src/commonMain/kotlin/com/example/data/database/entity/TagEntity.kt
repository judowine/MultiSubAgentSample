package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * Room entity representing a tag in the local database.
 * Maps to the "tags" table.
 *
 * Implementation by: data-layer-architect (coordinated by project-orchestrator)
 * PBI-5, Task 1.14: TagEntity for Meeting Notes & Tagging
 *
 * Tags are reusable labels that can be applied to multiple meeting records
 * via the many-to-many relationship defined in MeetingRecordTagCrossRef.
 *
 * The UNIQUE constraint on name ensures that tag names are not duplicated,
 * enabling tag autocomplete and suggestion features.
 *
 * @property id Primary key (auto-generated, local database ID)
 * @property name Tag name (unique, case-sensitive)
 * @property createdAt Timestamp when the tag was first created
 */
@Entity(
    tableName = "tags",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,           // tag name (unique constraint)
    val createdAt: Instant      // when tag was created
)
