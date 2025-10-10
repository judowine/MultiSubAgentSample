package org.example.project.judowine.navigation

import android.os.Bundle

/**
 * Android implementation: extracts arguments from Bundle.
 */
actual fun Any?.getLongArgument(key: String): Long? {
    return (this as? Bundle)?.getLong(key)
}

actual fun Any?.getStringArgument(key: String): String? {
    return (this as? Bundle)?.getString(key)
}
