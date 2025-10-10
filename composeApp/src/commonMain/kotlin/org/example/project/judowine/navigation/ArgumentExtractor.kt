package org.example.project.judowine.navigation

/**
 * Platform-specific argument extraction from NavBackStackEntry.arguments.
 *
 * On Android: arguments is Bundle, use Bundle.get(key)
 * On other platforms: adapt as needed
 */
expect fun Any?.getLongArgument(key: String): Long?
expect fun Any?.getStringArgument(key: String): String?
