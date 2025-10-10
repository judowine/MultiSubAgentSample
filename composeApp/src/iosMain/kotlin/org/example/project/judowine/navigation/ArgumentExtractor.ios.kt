package org.example.project.judowine.navigation

/**
 * iOS implementation: uses runtime introspection to access arguments.
 * Similar to JVM, uses reflection to access the arguments object.
 */
actual fun Any?.getLongArgument(key: String): Long? {
    // iOS navigation arguments handling
    // In Compose Multiplatform for iOS, arguments should work similarly
    if (this == null) return null

    // Try to access as a map-like structure or use KClass members
    return try {
        // Access using Kotlin reflection
        this::class.members
            .find { it.name == "get" }
            ?.call(this, key) as? Long
    } catch (e: Exception) {
        null
    }
}

actual fun Any?.getStringArgument(key: String): String? {
    if (this == null) return null

    return try {
        // Access using Kotlin reflection
        this::class.members
            .find { it.name == "get" }
            ?.call(this, key) as? String
    } catch (e: Exception) {
        null
    }
}
