package com.example.data.network

import com.example.data.network.dto.EventsResponse
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
 *
 * connpass API Documentation: https://connpass.com/about/api/
 * Base URL: https://connpass.com/api/v1/
 *
 * Features:
 * - JSON content negotiation with kotlinx.serialization
 * - Lenient JSON parsing (ignoreUnknownKeys for API evolution)
 * - Platform-specific engines (Android: OkHttp, iOS: Darwin, JVM: OkHttp)
 */
class ConnpassApiClient {
    /**
     * connpass API base URL
     */
    private val baseUrl = "https://connpass.com/api/v1"

    /**
     * Configured HTTP client with JSON serialization.
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
     * Get events from connpass API with optional filters.
     *
     * API Endpoint: GET /event/
     * Documentation: https://connpass.com/about/api/#eventapi
     *
     * @param userId connpass user ID to filter events by participant (optional)
     * @param count Maximum number of events to retrieve (default: 100, max: 100)
     * @param offset Pagination offset (default: 0)
     * @param order Sort order: 1 (updated_at asc), 2 (updated_at desc), 3 (started_at asc), 4 (started_at desc - default)
     * @return EventsResponse containing list of events and result metadata
     * @throws ApiException if API request fails
     * @throws NetworkException if network error occurs
     */
    suspend fun getEvents(
        userId: Long? = null,
        count: Int = 100,
        offset: Int = 0,
        order: Int = 4  // started_at DESC (newest first)
    ): EventsResponse {
        return try {
            httpClient.get("$baseUrl/event/") {
                parameter("count", count)
                parameter("start", offset + 1)  // connpass API is 1-indexed
                parameter("order", order)
                userId?.let { parameter("userId", it) }
            }.body()
        } catch (e: Exception) {
            // Error handling will be refined in Task 2.6
            throw ApiException("Failed to fetch events: ${e.message}", e)
        }
    }

    /**
     * Search events by keyword.
     *
     * @param keyword Search keyword (searches title, description, etc.)
     * @param count Maximum number of results
     * @param offset Pagination offset
     * @return EventsResponse containing matching events
     */
    suspend fun searchEvents(
        keyword: String,
        count: Int = 100,
        offset: Int = 0
    ): EventsResponse {
        return try {
            httpClient.get("$baseUrl/event/") {
                parameter("keyword", keyword)
                parameter("count", count)
                parameter("start", offset + 1)
                parameter("order", 4)  // newest first
            }.body()
        } catch (e: Exception) {
            throw ApiException("Failed to search events: ${e.message}", e)
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
