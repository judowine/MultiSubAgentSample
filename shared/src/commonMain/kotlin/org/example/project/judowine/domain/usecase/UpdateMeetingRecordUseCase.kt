package org.example.project.judowine.domain.usecase

import com.example.data.repository.MeetingRecordRepository

/**
 * Use Case for updating an existing meeting record with notes and tags.
 *
 * Implementation by: tactical-ddd-shared-implementer (coordinated by project-orchestrator)
 * PBI-5, Task 8.4: UpdateMeetingRecordUseCase
 *
 * This Application Service orchestrates the meeting record update operation,
 * following the DDD pattern of separating domain logic from infrastructure concerns.
 *
 * Responsibilities:
 * - Validate input parameters
 * - Filter out blank tag names
 * - Delegate to repository for persistence
 * - Return Result for error handling
 *
 * Business Rules:
 * - id must be positive (valid database ID)
 * - notes can be null or empty (optional)
 * - tagNames can be empty list (remove all tags)
 * - Blank tag names are filtered out before persistence
 *
 * @property meetingRecordRepository Repository for meeting record persistence
 */
class UpdateMeetingRecordUseCase(
    private val meetingRecordRepository: MeetingRecordRepository
) {
    /**
     * Updates an existing meeting record with notes and tags.
     *
     * This use case validates input and orchestrates the repository call.
     * All existing tags are replaced with the provided tag names.
     *
     * @param id The meeting record database ID (must be positive)
     * @param notes Optional text memo about the meeting (can be null or empty)
     * @param tagNames List of tag names to associate (blank names are filtered out)
     * @return Result.success(Unit) if updated successfully, Result.failure if error occurred
     */
    suspend operator fun invoke(
        id: Long,
        notes: String?,
        tagNames: List<String>
    ): Result<Unit> {
        // Validate input parameters
        if (id <= 0) {
            return Result.failure(
                IllegalArgumentException("Meeting record ID must be positive, got: $id")
            )
        }

        // Filter out blank tag names (defensive programming)
        val validTagNames = tagNames.filter { it.isNotBlank() }

        // Delegate to repository for persistence
        return meetingRecordRepository.updateMeetingRecord(
            id = id,
            notes = notes,
            tagNames = validTagNames
        )
    }
}
