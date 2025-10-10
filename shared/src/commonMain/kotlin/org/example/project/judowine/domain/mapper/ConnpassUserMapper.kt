package org.example.project.judowine.domain.mapper

import com.example.data.network.dto.UserDto
import org.example.project.judowine.domain.model.ConnpassUser

/**
 * Mapper functions to convert UserDto (API response) to ConnpassUser domain model.
 *
 * Implementation by: tactical-ddd-shared-implementer
 * PBI-3, Task 5.2: UserDto → ConnpassUser mapper
 *
 * Following the architecture rule: composeApp → shared → data
 * - /data module returns UserDto (has no knowledge of domain models)
 * - /shared module handles conversion via these mappers
 * - Use Cases in /shared orchestrate the mapping
 *
 * Key transformations:
 * - Empty strings → null (normalize empty values to null for nullable fields)
 * - Blank description/imageUrl → null (treat blank as absent)
 *
 * Fixed: Field names updated to match actual API response
 * - profile → description (API field name)
 * - iconUrl → imageUrl (API field name)
 * - connpassUrl → url (API field name)
 * - Removed twitterScreenName and githubUsername (not in actual API response)
 */

/**
 * Converts UserDto (API response) to ConnpassUser domain model.
 *
 * Handles nullable fields and normalizes empty/blank strings to null
 * for consistency in the domain model.
 *
 * @receiver UserDto from the connpass API
 * @return ConnpassUser domain model
 * @throws IllegalArgumentException if domain invariants are violated
 */
fun UserDto.toDomainModel(): ConnpassUser {
    return ConnpassUser(
        userId = userId,
        nickname = nickname,
        displayName = displayName,
        description = description?.takeIf { it.isNotBlank() },
        imageUrl = imageUrl?.takeIf { it.isNotBlank() },
        url = url
    )
}

/**
 * Converts a list of UserDto to a list of ConnpassUser domain models.
 *
 * Useful for batch conversion of search results from the API.
 *
 * @receiver List of UserDto from the connpass API
 * @return List of ConnpassUser domain models
 */
fun List<UserDto>.toDomainModels(): List<ConnpassUser> {
    return map { it.toDomainModel() }
}
