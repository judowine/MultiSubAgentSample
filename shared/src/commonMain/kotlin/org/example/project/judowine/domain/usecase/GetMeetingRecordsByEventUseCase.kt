package org.example.project.judowine.domain.usecase

import com.example.data.repository.MeetingRecordRepository
import org.example.project.judowine.domain.mapper.toDomainModels
import org.example.project.judowine.domain.model.MeetingRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case for retrieving meeting records for a specific event.
 *
 * Implementation by: tactical-ddd-shared-implementer (coordinated by project-orchestrator)
 * PBI-4, Task 7.5: GetMeetingRecordsByEventUseCase
 *
 * This use case orchestrates the business logic for fetching meeting records filtered by event ID
 * and handles conversion from data layer types (MeetingRecordEntity) to domain models (MeetingRecord).
 *
 * Use Case:
 * - Display "People I met at this event" on event detail screen
 * - Track networking connections made at specific events
 * - Build social graphs for event-based networking
 *
 * Flow Behavior:
 * - Returns a reactive Flow that emits updates whenever meeting records for this event change
 * - UI can collect this Flow to reactively update the meeting records list
 * - Records are ordered by creation date (newest first) via repository layer
 *
 * @property meetingRecordRepository The repository for meeting record data access (injected from /data module)
 */
class GetMeetingRecordsByEventUseCase(
    private val meetingRecordRepository: MeetingRecordRepository
) {
    /**
     * Get all meeting records for a specific event as a reactive Flow.
     *
     * This method orchestrates the following domain logic:
     * 1. Validates eventId (must be positive)
     * 2. Fetches meeting records filtered by eventId from the repository as a Flow
     * 3. Converts data layer types (MeetingRecordEntity) to domain models (MeetingRecord) using mapper
     * 4. Returns Flow for reactive UI updates
     *
     * The Flow will emit a new list whenever:
     * - A new meeting record is added for this event
     * - A meeting record for this event is deleted
     * - Meeting record data for this event is modified
     *
     * @param eventId The connpass event ID to filter by (must be positive)
     * @return Flow emitting list of MeetingRecord domain models for this event, ordered by createdAt DESC (newest first)
     * @throws IllegalArgumentException if eventId is not positive
     */
    fun execute(eventId: Long): Flow<List<MeetingRecord>> {
        require(eventId > 0) { "Event ID must be positive" }

        return meetingRecordRepository.getMeetingRecordsByEvent(eventId)
            .map { entities ->
                // Convert data layer types to domain models
                entities.toDomainModels()
            }
    }
}
