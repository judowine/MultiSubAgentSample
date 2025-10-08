This is a Kotlin Multiplatform project targeting Android, iOS, and Desktop (JVM).

## Architecture

This project follows **Android's official architecture guidelines** with strict layer isolation:

**Dependency Flow:**
```
composeApp → shared → data
```

**⚠️ CRITICAL RULE: Layer Isolation**
- `/composeApp` (Presentation) **MUST NOT** directly import or use ANY classes from `/data` module
- All data access **MUST** go through `/shared` (Domain layer) via Use Cases
- This ensures proper separation of concerns and maintainability

## Module Structure

* **[/composeApp](./composeApp/src)** - Presentation Layer (UI)
  - [commonMain](./composeApp/src/commonMain/kotlin) - Shared UI code for all targets
  - [commonTest](./composeApp/src/commonTest/kotlin) - Shared UI tests
  - [androidMain](./composeApp/src/androidMain/kotlin) - Android-specific entry point (MainActivity)
  - [jvmMain](./composeApp/src/jvmMain/kotlin) - Desktop (JVM) entry point
  - Package: `org.example.project.judowine`
  - **Depends on**: `/shared` module ONLY
  - **Data Access**: ONLY through Use Cases from `/shared` module

* **[/shared](./shared/src)** - Domain Layer (Business Logic)
  - [commonMain](./shared/src/commonMain/kotlin) - Platform-agnostic business logic
  - [commonTest](./shared/src/commonTest/kotlin) - Shared tests
  - [androidMain](./shared/src/androidMain/kotlin) / [iosMain](./shared/src/iosMain/kotlin) / [jvmMain](./shared/src/jvmMain/kotlin) - Platform-specific implementations
  - Package: `org.example.project.judowine`
  - **Depends on**: `/data` module
  - **Responsibilities**: Domain models (pure Kotlin), Use Cases, business rules

* **[/data](./data/src)** - Data Layer (Data Sources & Repositories)
  - [commonMain](./data/src/commonMain/kotlin) - Data layer with Ktor HTTP client and Room database
  - [androidMain](./data/src/androidMain/kotlin) - Android-specific data implementations (Ktor Android engine)
  - [iosMain](./data/src/iosMain/kotlin) - iOS-specific data implementations (Ktor Darwin engine)
  - [jvmMain](./data/src/jvmMain/kotlin) - JVM-specific data implementations (Ktor OkHttp engine)
  - [androidHostTest](./data/src/androidHostTest/kotlin) - Android host tests
  - [androidDeviceTest](./data/src/androidDeviceTest/kotlin) - Android device/instrumentation tests
  - Package: `com.example.data`
  - Framework name (iOS): `dataKit`
  - **Depends on**: NO other modules (leaf node)

* **[/iosApp](./iosApp)** - iOS native application wrapper
  - Contains SwiftUI views that consume the shared Kotlin framework
  - Imports the `Shared` framework from the shared module

## Setup

### API Keys Configuration

This project uses the connpass API. To configure your API key:

1. Create a `local.properties` file in the project root (if it doesn't exist)
2. Add your connpass API key:
   ```properties
   connpass.api.key=YOUR_API_KEY_HERE
   ```

**Note:** The `local.properties` file is ignored by git and should never be committed to version control.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:run
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…