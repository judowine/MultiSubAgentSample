package com.example.data.network.error

/**
 * Sealed interface for API error handling.
 * Provides type-safe error categories for repository and domain layer.
 *
 * Implementation by: project-orchestrator (coordinated by data-layer-architect)
 * PBI-2, Task 2.6: Error handling strategy for API failures
 *
 * Error Categories:
 * - NetworkError: Connection issues, timeouts, DNS failures
 * - ApiError: HTTP errors (4xx, 5xx), malformed responses
 * - ParseError: JSON deserialization failures
 * - UnknownError: Unexpected exceptions
 *
 * Usage:
 * ```kotlin
 * when (val error = result.exceptionOrNull()) {
 *     is NetworkError -> // Show "No internet connection" message
 *     is ApiError.NotFound -> // Show "Event not found" message
 *     is ApiError.ServerError -> // Show "Service temporarily unavailable"
 *     else -> // Show generic error message
 * }
 * ```
 */
sealed interface DataError {
    /**
     * Error message for display or logging
     */
    val message: String

    /**
     * Original exception (if available) for debugging
     */
    val cause: Throwable?
}

/**
 * Network connectivity errors.
 * Indicates issues with network infrastructure (no internet, timeout, DNS, etc.)
 *
 * UI Recommendation: Show "Check your internet connection" message with retry option
 */
data class NetworkError(
    override val message: String,
    override val cause: Throwable? = null
) : DataError

/**
 * API-specific errors (HTTP errors, malformed responses).
 */
sealed class ApiError(
    override val message: String,
    override val cause: Throwable? = null
) : DataError {

    /**
     * HTTP 400 Bad Request - Invalid API parameters
     */
    data class BadRequest(
        override val message: String = "Invalid request parameters",
        override val cause: Throwable? = null
    ) : ApiError(message, cause)

    /**
     * HTTP 401 Unauthorized - API key missing or invalid (if connpass adds auth)
     */
    data class Unauthorized(
        override val message: String = "Authentication required",
        override val cause: Throwable? = null
    ) : ApiError(message, cause)

    /**
     * HTTP 404 Not Found - Resource doesn't exist
     */
    data class NotFound(
        override val message: String = "Resource not found",
        override val cause: Throwable? = null
    ) : ApiError(message, cause)

    /**
     * HTTP 429 Too Many Requests - Rate limit exceeded
     */
    data class RateLimitExceeded(
        override val message: String = "Too many requests. Please try again later.",
        override val cause: Throwable? = null
    ) : ApiError(message, cause)

    /**
     * HTTP 5xx Server Error - connpass server issues
     */
    data class ServerError(
        override val message: String = "Server error. Please try again later.",
        override val cause: Throwable? = null
    ) : ApiError(message, cause)

    /**
     * Generic API error (unknown HTTP status or error code)
     */
    data class Unknown(
        override val message: String,
        override val cause: Throwable? = null
    ) : ApiError(message, cause)
}

/**
 * JSON parsing/deserialization errors.
 * Indicates API response format has changed or is malformed.
 *
 * UI Recommendation: Show "Service temporarily unavailable" (avoid exposing parsing errors to users)
 */
data class ParseError(
    override val message: String,
    override val cause: Throwable? = null
) : DataError

/**
 * Unknown/unexpected errors not covered by other categories.
 */
data class UnknownError(
    override val message: String,
    override val cause: Throwable? = null
) : DataError
