package org.example.project.judowine.domain.usecase

import com.example.data.repository.MeetingRecordRepository

/**
 * Use Case for deleting an existing meeting record by its ID.
 *
 * Implementation by: tactical-ddd-shared-implementer (coordinated by project-orchestrator)
 * PBI-5, Task 8.5: DeleteMeetingRecordUseCase
 *
 * This Application Service orchestrates the meeting record deletion operation,
 * following the DDD pattern of separating domain logic from infrastructure concerns.
 *
 * Responsibilities:
 * - Validate input parameters
 * - Delegate to repository for deletion
 * - Return Result for error handling
 *
 * Business Rules:
 * - id must be positive (valid database ID)
 * - Foreign key CASCADE will automatically delete associated tag cross-refs
 *
 * Note: This use case deletes by ID rather than by entity object,
 * which is more convenient for UI operations that only have the ID available.
 *
 * @property meetingRecordRepository Repository for meeting record persistence
 */
class DeleteMeetingRecordUseCase(
    private val meetingRecordRepository: MeetingRecordRepository
) {
    /**
     * Deletes an existing meeting record by its database ID.
     *
     * This use case validates input and orchestrates the repository call.
     * All associated tag cross-references are automatically deleted via CASCADE.
     *
     * @param id The meeting record database ID (must be positive)
     * @return Result.success(Unit) if deleted successfully, Result.failure if error occurred
     */
    suspend operator fun invoke(id: Long): Result<Unit> {
        // Validate input parameters
        if (id <= 0) {
            return Result.failure(
                IllegalArgumentException("Meeting record ID must be positive, got: $id")
            )
        }

        // Delegate to repository for deletion
        return meetingRecordRepository.deleteMeetingRecordById(id)
    }
}
