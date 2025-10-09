package org.example.project.judowine.domain.usecase

import com.example.data.repository.MeetingRecordRepository
import org.example.project.judowine.domain.mapper.toDomainModels
import org.example.project.judowine.domain.model.MeetingRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case for retrieving all meeting records from the repository.
 *
 * Implementation by: tactical-ddd-shared-implementer (coordinated by project-orchestrator)
 * PBI-4, Task 7.4: GetMeetingRecordsUseCase
 *
 * This use case orchestrates the business logic for fetching all meeting records
 * and handles conversion from data layer types (MeetingRecordEntity) to domain models (MeetingRecord).
 *
 * Flow Behavior:
 * - Returns a reactive Flow that emits updates whenever the database changes
 * - UI can collect this Flow to reactively update the meeting records list
 * - Records are ordered by creation date (newest first) via repository layer
 *
 * @property meetingRecordRepository The repository for meeting record data access (injected from /data module)
 */
class GetMeetingRecordsUseCase(
    private val meetingRecordRepository: MeetingRecordRepository
) {
    /**
     * Get all meeting records as a reactive Flow.
     *
     * This method orchestrates the following domain logic:
     * 1. Fetches all meeting records from the repository as a Flow
     * 2. Converts data layer types (MeetingRecordEntity) to domain models (MeetingRecord) using mapper
     * 3. Returns Flow for reactive UI updates
     *
     * The Flow will emit a new list whenever:
     * - A new meeting record is added
     * - A meeting record is deleted
     * - Meeting record data is modified
     *
     * @return Flow emitting list of MeetingRecord domain models, ordered by createdAt DESC (newest first)
     */
    fun execute(): Flow<List<MeetingRecord>> {
        return meetingRecordRepository.getAllMeetingRecords()
            .map { entities ->
                // Convert data layer types to domain models
                entities.toDomainModels()
            }
    }
}
