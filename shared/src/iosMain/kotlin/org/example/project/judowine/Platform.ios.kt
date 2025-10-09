package org.example.project.judowine

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

/**
 * Opens the specified URL in the default external browser (Safari).
 *
 * Uses UIApplication.sharedApplication.openURL() for iOS 10+
 *
 * @param url The URL to open (must be a valid HTTP/HTTPS URL)
 */
actual fun openUrl(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}