package com.example.data.network

import com.example.data.network.dto.EventsResponse
import com.example.data.network.dto.UsersResponseDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * HTTP client for connpass API integration.
 * Provides configured Ktor HttpClient for making API requests to connpass.com.
 *
 * Implementation by: project-orchestrator (coordinated by data-layer-architect)
 * PBI-2, Tasks 2.1-2.2: Ktor client setup with content negotiation
 * PBI-3, Task 2.7: Extended with user search functionality
 *
 * connpass API Documentation: https://connpass.com/about/api/
 * Base URL (v2): https://connpass.com/api/v2/ (users/groups/events API)
 *
 * Authentication: https://connpass.com/about/api/v2/#section/%E6%A6%82%E8%A6%81/%E8%AA%8D%E8%A8%BC
 * - ALL v2 API endpoints require X-API-Key header authentication
 * - API key is injected from local.properties (required)
 * - Apply for API key at: https://connpass.com/about/api/
 *
 * Example:
 * ```
 * curl -X GET "https://connpass.com/api/v2/events/?keyword=python" \
 *   -H "X-API-Key: YOUR_API_KEY"
 * ```
 *
 * Features:
 * - JSON content negotiation with kotlinx.serialization
 * - Lenient JSON parsing (ignoreUnknownKeys for API evolution)
 * - Platform-specific engines (Android: OkHttp, iOS: Darwin, JVM: OkHttp)
 *
 * @param apiKey connpass API key for authentication (required for all v2 endpoints)
 */
class ConnpassApiClient(private val apiKey: String) {
    /**
     * connpass API base URL (v2)
     */
    private val baseUrl = "https://connpass.com/api/v2"

    /**
     * Configured HTTP client with JSON serialization and authentication.
     * Platform-specific engines are configured via Ktor's engine auto-selection.
     */
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true  // Ignore API fields we don't need
                isLenient = true           // Allow flexible JSON parsing
                prettyPrint = false        // Compact JSON for network efficiency
            })
        }
    }

    /**
     * Add X-API-Key header to the request.
     * This helper function adds authentication to ALL API v2 endpoints.
     *
     * Note: ALL v2 endpoints require X-API-Key authentication as per official documentation.
     */
    private fun HttpRequestBuilder.addAuthHeader() {
        header("X-API-Key", apiKey)
    }

    /**
     * Search events by keyword.
     *
     * API Endpoint: GET /api/v2/events/
     * Documentation: https://connpass.com/about/api/#eventsapi
     * Authentication: Required (X-API-Key header)
     *
     * Note: This method uses v2 API with keyword parameter for searching events.
     * For user-specific events, use getEventsByNickname() instead.
     *
     * @param keyword Search keyword (searches title, description, etc.)
     * @param count Maximum number of results (default: 100, max: 100)
     * @param offset Pagination offset (default: 0)
     * @param order Sort order: 1 (updated_at), 2 (started_at), 3 (created_at - newest first)
     * @return EventsResponse containing matching events
     * @throws ApiException if API request fails
     */
    suspend fun searchEvents(
        keyword: String,
        count: Int = 100,
        offset: Int = 0,
        order: Int = 2  // started_at (upcoming events first)
    ): EventsResponse {
        return try {
            httpClient.get("$baseUrl/events/") {
                addAuthHeader()  // X-API-Key required for v2 API
                parameter("keyword", keyword)
                parameter("count", count)
                parameter("start", offset + 1)  // connpass API is 1-indexed
                parameter("order", order)
            }.body()
        } catch (e: Exception) {
            throw ApiException("Failed to search events: ${e.message}", e)
        }
    }

    /**
     * Search users by nickname.
     *
     * API Endpoint: GET /api/v2/users/
     * Documentation: https://connpass.com/about/api/#usersapi
     * Authentication: Required (X-API-Key header)
     *
     * @param nickname User's nickname to search for (partial match supported)
     * @param start Pagination start index (1-based, default: 1)
     * @param count Maximum number of users to retrieve (default: 100, max: 100)
     * @return UsersResponseDto containing list of matching users and result metadata
     * @throws ApiException if API request fails
     */
    suspend fun searchUsers(
        nickname: String,
        start: Int = 1,
        count: Int = 100
    ): UsersResponseDto {
        return try {
            httpClient.get("$baseUrl/users/") {
                addAuthHeader()  // X-API-Key required for v2 API
                parameter("nickname", nickname)
                parameter("start", start)
                parameter("count", count)
            }.body()
        } catch (e: Exception) {
            throw ApiException("Failed to search users: ${e.message}", e)
        }
    }

    /**
     * Get events by user's nickname (events the user has participated in).
     *
     * API Endpoint: GET /api/v2/events/?nickname=...
     * Documentation: https://connpass.com/about/api/#eventsapi
     * Authentication: Required (X-API-Key header)
     *
     * @param nickname connpass user's nickname to filter events by participant
     * @param count Maximum number of events to retrieve (default: 50, max: 100)
     * @param start Pagination start index (1-based, default: 1)
     * @param order Sort order: 1 (updated_at), 2 (started_at), 3 (created_at - newest first default)
     * @return EventsResponse containing list of events the user participated in
     * @throws ApiException if API request fails
     */
    suspend fun getEventsByNickname(
        nickname: String,
        count: Int = 50,
        start: Int = 1,
        order: Int = 2  // started_at (upcoming events first)
    ): EventsResponse {
        return try {
            httpClient.get("$baseUrl/events/") {
                addAuthHeader()  // X-API-Key required for v2 API
                parameter("nickname", nickname)
                parameter("count", count)
                parameter("start", start)
                parameter("order", order)
            }.body()
        } catch (e: Exception) {
            throw ApiException("Failed to fetch events for user: ${e.message}", e)
        }
    }

    /**
     * Close the HTTP client.
     * Should be called when the client is no longer needed (e.g., app shutdown).
     */
    fun close() {
        httpClient.close()
    }
}

/**
 * Temporary exception class for API errors.
 * Will be refined in Task 2.6 with proper error handling strategy.
 */
class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)
