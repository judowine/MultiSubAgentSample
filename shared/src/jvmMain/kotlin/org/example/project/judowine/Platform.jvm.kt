package org.example.project.judowine

import java.awt.Desktop
import java.net.URI

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

/**
 * Opens the specified URL in the default external browser.
 *
 * Uses java.awt.Desktop.browse() for JVM/Desktop platform.
 *
 * @param url The URL to open (must be a valid HTTP/HTTPS URL)
 */
actual fun openUrl(url: String) {
    if (Desktop.isDesktopSupported()) {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(URI(url))
        }
    }
}