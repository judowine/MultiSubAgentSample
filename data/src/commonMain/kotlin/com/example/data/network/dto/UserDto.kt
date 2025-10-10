package com.example.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for connpass API user search responses.
 * Maps to the JSON structure returned by connpass API /users/ endpoint.
 *
 * Implementation by: data-layer-architect
 * PBI-3, Task 2.8: UserDto for connpass user search API response model
 *
 * connpass API Documentation: https://connpass.com/about/api/#usersapi
 * API Endpoint: GET https://connpass.com/api/v2/users/
 *
 * Note: This DTO is for SEARCHING OTHER USERS (not user's own profile - see UserEntity for that).
 * No local database persistence (API-only, as per PBI-3 scope).
 */

/**
 * API response wrapper containing user search results and metadata.
 *
 * @property resultsStart Starting index (1-based pagination)
 * @property resultsReturned Number of users in this response
 * @property resultsAvailable Total number of matching users
 * @property users List of user data
 */
@Serializable
data class UsersResponseDto(
    @SerialName("results_start")
    val resultsStart: Int,

    @SerialName("results_returned")
    val resultsReturned: Int,

    @SerialName("results_available")
    val resultsAvailable: Int,

    @SerialName("users")
    val users: List<UserDto>
)

/**
 * User data transfer object for connpass user search.
 * Maps connpass API user fields to Kotlin data class.
 *
 * Field mapping (API → DTO):
 * - id → userId (unique identifier)
 * - nickname → nickname (login name)
 * - display_name → displayName (public display name)
 * - description → description (bio/description, may be HTML)
 * - image_url → imageUrl (profile image URL)
 * - url → url (user profile page)
 *
 * @property userId connpass user ID (unique identifier)
 * @property nickname User's login name/nickname
 * @property displayName User's public display name
 * @property description User bio/profile description (may contain HTML tags, nullable)
 * @property imageUrl Profile image URL (nullable)
 * @property url User's connpass profile page URL
 */
@Serializable
data class UserDto(
    @SerialName("id")
    val userId: Int,

    @SerialName("nickname")
    val nickname: String,

    @SerialName("display_name")
    val displayName: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("url")
    val url: String
)
