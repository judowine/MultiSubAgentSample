package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * Room entity representing a connpass event in the local database.
 * Maps to the "events" table.
 *
 * Implementation by: data-layer-architect (coordinated by project-orchestrator)
 * PBI-2, Tasks 1.6-1.8: Event entity for Event Discovery & Viewing
 *
 * This entity stores events fetched from the connpass API for offline access
 * and caching. Fields map directly to connpass API response structure.
 *
 * @property id Primary key (auto-generated, local database ID)
 * @property eventId connpass event ID (unique identifier from connpass API)
 * @property title Event title/name
 * @property description Event description (full text, may be long)
 * @property startedAt Event start date and time
 * @property endedAt Event end date and time
 * @property url Event URL on connpass
 * @property address Event venue address (physical location)
 * @property limit Maximum number of participants (0 for unlimited)
 * @property accepted Number of accepted participants
 * @property waiting Number of participants on waiting list
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: Long,           // connpass event ID
    val title: String,
    val description: String,
    val startedAt: Instant,
    val endedAt: Instant,
    val url: String,
    val address: String,
    val limit: Int,              // 0 means unlimited
    val accepted: Int,
    val waiting: Int
)
