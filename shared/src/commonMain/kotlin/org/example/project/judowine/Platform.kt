package org.example.project.judowine

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

/**
 * Opens the specified URL in the default external browser.
 *
 * Platform-specific implementation:
 * - Android: Uses Intent with ACTION_VIEW
 * - iOS: Uses UIApplication.shared.openURL
 * - JVM/Desktop: Uses java.awt.Desktop.browse
 *
 * @param url The URL to open (must be a valid HTTP/HTTPS URL)
 */
expect fun openUrl(url: String)