package org.example.project.judowine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

/**
 * Global application context for Android platform utilities.
 * Must be initialized in Application.onCreate() before using openUrl().
 */
private var applicationContext: Context? = null

/**
 * Initialize the global application context.
 * Call this from Application.onCreate().
 *
 * @param context The application context
 */
fun initializeAndroidContext(context: Context) {
    applicationContext = context.applicationContext
}

/**
 * Opens the specified URL in the default external browser.
 *
 * @param url The URL to open (must be a valid HTTP/HTTPS URL)
 * @throws IllegalStateException if applicationContext is not initialized
 */
actual fun openUrl(url: String) {
    val context = applicationContext ?: throw IllegalStateException(
        "Application context not initialized. Call initializeAndroidContext() in Application.onCreate()"
    )

    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}