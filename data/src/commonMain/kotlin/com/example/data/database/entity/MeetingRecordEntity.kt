package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * Room entity representing a meeting record in the local database.
 * Maps to the "meeting_records" table.
 *
 * Implementation by: data-layer-architect (coordinated by project-orchestrator)
 * PBI-4, Task 1.9: MeetingRecordEntity for Meeting Record Creation
 * PBI-5, Task 1.13: Added notes field for Meeting Notes & Tagging
 *
 * This entity stores records of people met at specific events, enabling users
 * to track their networking and connections at connpass events.
 *
 * The UNIQUE constraint on (eventId, userId) prevents duplicate records for the
 * same person at the same event.
 *
 * @property id Primary key (auto-generated, local database ID)
 * @property eventId connpass event ID (foreign key to events table)
 * @property userId connpass user ID (the person you met)
 * @property nickname User's nickname (cached from API for display)
 * @property notes Optional text memo about the meeting/conversation (PBI-5)
 * @property createdAt Timestamp when the meeting record was created
 */
@Entity(
    tableName = "meeting_records",
    indices = [
        Index(value = ["eventId", "userId"], unique = true)
    ]
)
data class MeetingRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: Long,          // FK to events.eventId
    val userId: Long,           // connpass user ID
    val nickname: String,       // cached user nickname
    val notes: String? = null,  // optional meeting notes (PBI-5)
    val createdAt: Instant      // when record was created
)
