package org.example.project.judowine.navigation

/**
 * Navigation Routes for EventMeet Application
 *
 * Implementation: Navigation system implementation (PBI-8)
 * Architecture: Type-safe navigation using sealed class hierarchy
 *
 * Route Definitions:
 * - Sealed class ensures compile-time safety
 * - Each route corresponds to a screen in the application
 * - Parameters are strongly typed and documented
 *
 * Usage:
 * ```
 * navController.navigate(Routes.EventDetail(eventId = 123).route)
 * ```
 */
sealed class Routes(val route: String) {

    // ========== Main Navigation (PBI-8) ==========

    /**
     * Main Screen with Bottom Navigation
     * Container screen with tabs for Events, People, and Profile
     * No parameters required
     */
    data object Main : Routes("main")

    /**
     * Main Events Tab
     * Shows EventList within Main screen's Events tab
     * No parameters required
     */
    data object MainEvents : Routes("main/events")

    /**
     * Main People Tab
     * Shows PeopleList within Main screen's People tab
     * No parameters required
     */
    data object MainPeople : Routes("main/people")

    /**
     * Main Profile Tab
     * Shows ProfileDisplay within Main screen's Profile tab
     * No parameters required
     */
    data object MainProfile : Routes("main/profile")

    // ========== Profile Management (PBI-1) ==========

    /**
     * Profile Registration Screen
     * Initial screen for new users to register their profile
     * No parameters required
     */
    data object ProfileRegistration : Routes("profile/registration")

    /**
     * Profile Display Screen
     * Shows the current user's profile information
     * No parameters required
     */
    data object ProfileDisplay : Routes("profile/display")

    /**
     * Profile Edit Screen
     * Allows user to edit their profile information
     * No parameters required
     */
    data object ProfileEdit : Routes("profile/edit")

    // ========== Event Discovery & Viewing (PBI-2) ==========

    /**
     * Event List Screen
     * Displays all available events from Connpass
     * No parameters required
     */
    data object EventList : Routes("events")

    /**
     * Event Detail Screen
     * Shows detailed information for a specific event
     * @param eventId Connpass event ID (Long)
     * Route: "events/{eventId}"
     */
    data class EventDetail(val eventId: Long) : Routes("events/$eventId") {
        companion object {
            const val ROUTE_PATTERN = "events/{eventId}"
            const val ARG_EVENT_ID = "eventId"
        }
    }

    // ========== User Search & Profile (PBI-3) ==========

    /**
     * User Search Screen
     * Allows searching for Connpass users
     * No parameters required
     */
    data object UserSearch : Routes("users/search")

    /**
     * User Detail Screen
     * Shows profile information for a specific Connpass user
     * @param nickname Connpass user nickname (String)
     * Route: "users/{nickname}"
     */
    data class UserDetail(val nickname: String) : Routes("users/$nickname") {
        companion object {
            const val ROUTE_PATTERN = "users/{nickname}"
            const val ARG_NICKNAME = "nickname"
        }
    }

    // ========== Meeting Record Creation (PBI-4) ==========

    /**
     * Meeting Record List Screen
     * Displays all meeting records created by the user
     * No parameters required
     */
    data object MeetingRecordList : Routes("meeting-records")

    /**
     * Add Meeting Record Screen
     * Multi-step wizard to create a new meeting record
     * @param preSelectedEventId Optional event ID to pre-select (from EventDetail FAB)
     * Route: "meeting-records/add?eventId={eventId}"
     */
    data class AddMeetingRecord(val preSelectedEventId: Long? = null) : Routes(
        if (preSelectedEventId != null) "meeting-records/add?eventId=$preSelectedEventId"
        else "meeting-records/add"
    ) {
        companion object {
            const val ROUTE_PATTERN = "meeting-records/add?eventId={eventId}"
            const val ARG_EVENT_ID = "eventId"
        }
    }

    /**
     * Meeting Record Detail Screen
     * Shows full details of a specific meeting record
     * @param meetingRecordId Meeting record database ID (Long)
     * Route: "meeting-records/{meetingRecordId}"
     */
    data class MeetingRecordDetail(val meetingRecordId: Long) : Routes("meeting-records/$meetingRecordId") {
        companion object {
            const val ROUTE_PATTERN = "meeting-records/{meetingRecordId}"
            const val ARG_MEETING_RECORD_ID = "meetingRecordId"
        }
    }

    // ========== Meeting Notes & Tagging (PBI-5) ==========

    /**
     * Edit Meeting Record Screen
     * Allows editing notes and tags for an existing meeting record
     * @param meetingRecordId Meeting record database ID (Long)
     * Route: "meeting-records/{meetingRecordId}/edit"
     */
    data class EditMeetingRecord(val meetingRecordId: Long) : Routes("meeting-records/$meetingRecordId/edit") {
        companion object {
            const val ROUTE_PATTERN = "meeting-records/{meetingRecordId}/edit"
            const val ARG_MEETING_RECORD_ID = "meetingRecordId"
        }
    }

    // ========== People-Centric Meeting History (PBI-6) ==========

    /**
     * People List Screen
     * Displays all people the user has met (grouped by person)
     * No parameters required
     */
    data object PeopleList : Routes("people")

    /**
     * Person Detail Screen
     * Shows all meetings with a specific person
     * @param userId Connpass user ID (Long)
     * Route: "people/{userId}"
     */
    data class PersonDetail(val userId: Long) : Routes("people/$userId") {
        companion object {
            const val ROUTE_PATTERN = "people/{userId}"
            const val ARG_USER_ID = "userId"
        }
    }

    companion object {
        /**
         * Helper function to extract Long argument from NavBackStackEntry
         * @param key Argument key name
         * @param arguments Bundle containing navigation arguments
         * @return Long value or null if not found
         */
        fun getLongArg(key: String, arguments: Map<String, String>?): Long? {
            return arguments?.get(key)?.toLongOrNull()
        }

        /**
         * Helper function to extract optional Long argument from NavBackStackEntry
         * Used for optional query parameters like preSelectedEventId
         * @param key Argument key name
         * @param arguments Bundle containing navigation arguments
         * @return Long value or null if not found or invalid
         */
        fun getOptionalLongArg(key: String, arguments: Map<String, String>?): Long? {
            val value = arguments?.get(key)
            return if (value.isNullOrEmpty() || value == "{$key}") null else value.toLongOrNull()
        }
    }
}
