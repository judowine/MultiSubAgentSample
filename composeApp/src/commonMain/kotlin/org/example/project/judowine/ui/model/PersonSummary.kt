package org.example.project.judowine.ui.model

import kotlinx.datetime.Instant

/**
 * UI model representing a summary of a person the user has met.
 *
 * Implementation by: compose-ui-architect (coordinated by project-orchestrator)
 * PBI-6, Task 1: PersonSummary UI model
 *
 * This is a **UI-specific model**, not a domain model. It is derived from
 * aggregating multiple MeetingRecord domain models by userId in the ViewModel.
 *
 * PersonSummary is used for:
 * - Displaying the people list (PeopleListScreen)
 * - Header information in person detail screen (PersonDetailScreen)
 * - Sorting and filtering people by meeting count or last meeting date
 *
 * Architecture Note:
 * - This model lives in the Presentation layer (/composeApp/ui/model)
 * - It is NOT a domain model (domain layer has no concept of "person aggregation")
 * - ViewModels derive this from List<MeetingRecord> using aggregation logic
 *
 * Example Derivation Logic (in ViewModel):
 * ```kotlin
 * val peopleList = meetingRecords
 *     .groupBy { it.userId }
 *     .map { (userId, records) ->
 *         PersonSummary(
 *             userId = userId,
 *             nickname = records.first().nickname,
 *             meetingCount = records.size,
 *             lastMeetingDate = records.maxOf { it.createdAt }
 *         )
 *     }
 *     .sortedByDescending { it.lastMeetingDate }
 * ```
 *
 * @property userId connpass user ID (unique identifier for the person)
 * @property nickname User's display name (from the most recent MeetingRecord)
 * @property meetingCount Total number of meetings with this person
 * @property lastMeetingDate Date of the most recent meeting with this person
 */
data class PersonSummary(
    val userId: Long,
    val nickname: String,
    val meetingCount: Int,
    val lastMeetingDate: Instant
)
