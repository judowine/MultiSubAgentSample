package org.example.project.judowine

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform