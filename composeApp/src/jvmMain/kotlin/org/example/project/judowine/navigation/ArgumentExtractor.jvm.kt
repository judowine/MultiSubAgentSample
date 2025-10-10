package org.example.project.judowine.navigation

/**
 * JVM implementation: uses reflection to access arguments.
 * Note: For desktop apps, navigation arguments should work similarly to Android.
 */
actual fun Any?.getLongArgument(key: String): Long? {
    if (this == null) return null
    return try {
        // Use reflection to call get(String) method
        val getMethod = this::class.java.getMethod("get", String::class.java)
        getMethod.invoke(this, key) as? Long
    } catch (e: Exception) {
        null
    }
}

actual fun Any?.getStringArgument(key: String): String? {
    if (this == null) return null
    return try {
        // Use reflection to call get(String) method
        val getMethod = this::class.java.getMethod("get", String::class.java)
        getMethod.invoke(this, key) as? String
    } catch (e: Exception) {
        null
    }
}
