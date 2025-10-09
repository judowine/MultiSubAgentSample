package org.example.project.judowine

import android.app.Application
import com.example.data.di.androidDataModule
import com.example.data.di.dataModule
import org.example.project.judowine.di.domainModule
import org.example.project.judowine.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application class for EventMeet with Koin Dependency Injection.
 *
 * Responsibilities:
 * - Initialize Koin DI framework on app startup
 * - Register all Koin modules (data, domain, presentation)
 *
 * Architecture Note:
 * - Uses Koin for dependency injection across all layers
 * - Data layer: androidDataModule + dataModule (Room, Repositories)
 * - Domain layer: domainModule (Use Cases)
 * - Presentation layer: presentationModule (ViewModels)
 * - UI components get dependencies via Koin's `koinViewModel()` or `koinInject()`
 * - Maintains layer isolation: composeApp → shared → data
 */
class EventMeetApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Android context for platform utilities (e.g., openUrl)
        initializeAndroidContext(this)

        // Initialize Koin DI
        startKoin {
            // Use Android Logger (Logcat)
            androidLogger(Level.ERROR)

            // Inject Android Context
            androidContext(this@EventMeetApplication)

            // Inject API keys from BuildConfig
            properties(
                mapOf(
                    "connpass.api.key" to BuildConfig.CONNPASS_API_KEY
                )
            )

            // Register all modules
            modules(
                // Data layer modules
                androidDataModule, // Android-specific (Room DB, DAOs)
                dataModule,        // Common data layer (Repositories)

                // Domain layer module
                domainModule,      // Use Cases

                // Presentation layer module
                presentationModule // ViewModels
            )
        }
    }
}
