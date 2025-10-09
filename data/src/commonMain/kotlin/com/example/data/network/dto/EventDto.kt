package com.example.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for connpass API event responses.
 * Maps to the JSON structure returned by connpass API /event/ endpoint.
 *
 * Implementation by: project-orchestrator (coordinated by data-layer-architect)
 * PBI-2, Task 2.3: EventDto for connpass API response model
 *
 * connpass API Documentation: https://connpass.com/about/api/#eventapi
 *
 * Note: This DTO contains only the fields needed for PBI-2 (Event Discovery & Viewing).
 * The connpass API returns many more fields which are ignored via ignoreUnknownKeys.
 */

/**
 * API response wrapper containing event results and metadata.
 *
 * @property resultsReturned Number of events in this response
 * @property resultsAvailable Total number of matching events
 * @property resultsStart Starting index (1-based pagination)
 * @property events List of event data
 */
@Serializable
data class EventsResponse(
    @SerialName("results_returned")
    val resultsReturned: Int,

    @SerialName("results_available")
    val resultsAvailable: Int,

    @SerialName("results_start")
    val resultsStart: Int,

    @SerialName("events")
    val events: List<EventDto>
)

/**
 * Event data transfer object.
 * Maps connpass API event fields to Kotlin data class.
 *
 * Field mapping (API → DTO):
 * - id → eventId
 * - title → title
 * - description → description (HTML)
 * - started_at → startedAt (ISO 8601 datetime string)
 * - ended_at → endedAt (ISO 8601 datetime string)
 * - url → eventUrl
 * - address → address
 * - limit → limit (0 for unlimited)
 * - accepted → accepted
 * - waiting → waiting
 *
 * @property eventId connpass event ID (unique identifier)
 * @property title Event title
 * @property description Event description (may contain HTML tags)
 * @property startedAt Event start datetime (ISO 8601 string: "2024-12-25T19:00:00+09:00")
 * @property endedAt Event end datetime (ISO 8601 string)
 * @property eventUrl Event page URL on connpass
 * @property address Event venue address (empty string if online event)
 * @property limit Maximum participants (0 = unlimited)
 * @property accepted Number of accepted participants
 * @property waiting Number of waiting list participants
 */
@Serializable
data class EventDto(
    @SerialName("id")
    val eventId: Long,

    @SerialName("title")
    val title: String,

    @SerialName("catch")
    val catch: String? = null,  // Short description/tagline

    @SerialName("description")
    val description: String,

    @SerialName("url")
    val eventUrl: String,

    @SerialName("started_at")
    val startedAt: String,  // ISO 8601 datetime string

    @SerialName("ended_at")
    val endedAt: String,    // ISO 8601 datetime string

    @SerialName("limit")
    val limit: Int? = null,  // null or 0 for unlimited

    @SerialName("accepted")
    val accepted: Int,

    @SerialName("waiting")
    val waiting: Int,

    @SerialName("updated_at")
    val updatedAt: String,  // ISO 8601 datetime string

    @SerialName("address")
    val address: String? = null,  // null for online events

    @SerialName("place")
    val place: String? = null,  // Venue name

    @SerialName("owner_id")
    val ownerId: Long,

    @SerialName("owner_nickname")
    val ownerNickname: String,

    @SerialName("owner_display_name")
    val ownerDisplayName: String
)
