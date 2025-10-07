# ADR-001: Architecture Pattern Selection for KMP Project

## Status
**Accepted** - 2025-10-07

## Context

This Kotlin Multiplatform project targets Android, iOS, and Desktop (JVM) platforms using Compose Multiplatform for shared UI. We needed to establish a clear architectural pattern to guide development as the project grows.

Two primary architectural patterns were considered:

1. **Clean Architecture** (domain-centric with dependency inversion)
2. **Android UDF Architecture** (unidirectional data flow with natural dependencies)

### Project Characteristics

- **Target Platforms**: Android, iOS, Desktop (JVM)
- **UI Framework**: Compose Multiplatform
- **Data Dependencies**: Room (KMP), Ktor Client (KMP), kotlinx-datetime
- **Module Structure**: `/composeApp`, `/shared`, `/data`
- **Key Insight**: All data layer dependencies are multiplatform-native (Room and Ktor support KMP)

### Decision Drivers

1. **Pragmatism vs. Purity**: Balance between theoretical architectural purity and practical development efficiency
2. **KMP Characteristics**: Leverage KMP-specific patterns (expect/actual) appropriately
3. **Team Productivity**: Minimize unnecessary complexity and boilerplate
4. **Maintainability**: Clear, understandable dependency graph
5. **Official Guidance**: Alignment with Android's official architecture recommendations
6. **Testability**: Ensure domain logic can be tested effectively

## Decision

**We adopt the Android UDF (Unidirectional Data Flow) architecture pattern with the following dependency structure:**

```
composeApp → shared → data
```

### Layer Responsibilities

| Layer | Module | Package | Responsibilities |
|-------|--------|---------|-----------------|
| **Presentation** | `/composeApp` | `org.example.project.judowine.ui` | UI components, ViewModels, navigation, user interaction |
| **Domain** | `/shared` | `org.example.project.judowine.domain` | Domain models (pure Kotlin), Use Cases, business rules, domain services |
| **Data** | `/data` | `com.example.data` | Repository interfaces & implementations, Room DAOs, Ktor clients, DTOs, mappers |

### Key Architectural Decisions

1. **Repository Pattern Placement**
   - **Interface**: Defined in `/data` module (NOT in `/shared`)
   - **Implementation**: Implemented in `/data` module (same module)
   - **Rationale**: Simplifies dependency injection and eliminates cross-module interface wiring

2. **Domain Model Isolation**
   - Domain models (entities, value objects) remain **pure Kotlin** in `/shared`
   - **Zero framework dependencies** in domain models
   - DTOs and domain models are separate types, connected via mappers in `/data`

3. **Dependency Direction**
   - `/shared` depends on `/data` (natural dependency flow)
   - `/composeApp` depends on `/shared` only
   - `/data` has NO dependencies on other modules (leaf node)

4. **Unidirectional Data Flow**
   - **Data Flow**: `data → domain → UI` (upward)
   - **Event Flow**: `UI → domain → data` (downward)
   - **Compile-time Dependency**: `composeApp → shared → data`
   - **Runtime Data Flow**: Data layer emits → Domain transforms → UI consumes

## Rationale

### Why Android UDF Over Clean Architecture?

#### 1. **Alignment with Official Android Guidance**

The Android architecture guide explicitly recommends:
- Domain layer depends on Data layer
- Repository implementations live in Data layer
- Focus on unidirectional data flow over dependency inversion

**Reference**: https://developer.android.com/topic/architecture

This pattern has been validated by Google's Android team through extensive real-world usage.

#### 2. **KMP Data Libraries Are Multiplatform**

All our data dependencies are KMP-native:
- **Room**: Multiplatform database with KSP support
- **Ktor**: Multiplatform HTTP client with platform-specific engines
- **kotlinx-datetime**: Multiplatform date/time library

**Implication**: There is NO platform-specific divergence in the data layer that would require dependency inversion. All platforms (Android, iOS, Desktop) use the same data layer implementation.

#### 3. **Simpler Development Experience**

**With Android UDF** (current approach):
```kotlin
// In /data module
interface UserRepository {
    fun getUsers(): Flow<List<User>>
}

class UserRepositoryImpl(
    private val database: AppDatabase
) : UserRepository {
    override fun getUsers() = database.userDao().getAllUsers()
}

// In /shared module - direct usage
class GetUsersUseCase(
    private val repository: UserRepository // Direct dependency
) {
    operator fun invoke() = repository.getUsers()
}
```

**With Clean Architecture** (rejected):
```kotlin
// In /shared module - interface definition
interface UserRepository {
    fun getUsers(): Flow<List<User>>
}

// In /data module - implementation
class UserRepositoryImpl(...) : UserRepository { ... }

// Now requires complex DI setup to wire /data implementation
// into /shared interfaces across module boundaries
// Requires Koin/Hilt or manual factory pattern
```

**Result**: Android UDF eliminates cross-module dependency injection complexity.

#### 4. **Unidirectional Data Flow Compatibility**

**Critical Insight**: UDF is about **runtime data flow**, not compile-time dependencies.

- **Compile-time**: `shared → data` (natural dependency)
- **Runtime**: Data emits → Domain transforms → UI consumes (unidirectional)
- **Events**: UI events → Domain logic → Data persistence (downward)

These concerns are **orthogonal**. You can achieve perfect UDF with `shared → data` dependencies.

#### 5. **Reduced Complexity Without Sacrificing Testability**

**Testing domain logic** remains straightforward:

```kotlin
// In /shared/commonTest
class GetUsersUseCaseTest {
    @Test
    fun `should transform user data correctly`() = runTest {
        val fakeRepository = FakeUserRepository() // Mock from /data module
        val useCase = GetUsersUseCase(fakeRepository)

        val result = useCase()

        // Assertions
    }
}
```

Mocking repository interfaces defined in `/data` is just as easy as mocking interfaces in `/shared`. The only difference is import location.

#### 6. **No Platform-Specific Data Implementation Divergence**

Clean Architecture shines when you have:
- iOS using CoreData
- Android using Room
- Desktop using SQLDelight

**Our Reality**: All platforms use Room KMP + Ktor KMP

There is **no platform-specific data layer variation** to abstract away, so dependency inversion provides no benefit.

### Why NOT Clean Architecture?

| Clean Architecture Requirement | KMP Reality | Impact |
|-------------------------------|-------------|--------|
| Data layer must implement domain interfaces | All platforms use same data layer (Room/Ktor KMP) | No benefit from inversion |
| Domain must be framework-agnostic | Domain models are pure Kotlin (no framework dependencies) | Already achieved without inversion |
| Supports multiple data layer implementations | Single shared data layer for all platforms | Multiple implementations unnecessary |
| Enables domain logic reuse outside mobile | Project is mobile-only (Android/iOS/Desktop) | No backend or external consumers |
| Requires dependency injection framework | Adds complexity (Koin/Hilt) for cross-module wiring | Unnecessary overhead |

**Conclusion**: Clean Architecture solves problems we don't have while adding complexity we don't need.

## Consequences

### Positive

1. **Simpler dependency graph**: Clear, unidirectional dependencies easy to understand
2. **Reduced boilerplate**: No cross-module interface/implementation separation
3. **Faster development**: Direct dependencies without DI framework setup
4. **Android alignment**: Follows official Android architecture guide
5. **Pragmatic approach**: Focuses on delivering value over architectural purity
6. **Easier onboarding**: New team members can understand structure quickly

### Negative

1. **Domain coupled to data module**: `/shared` depends on `/data` (compile-time coupling)
2. **Less portable domain**: Cannot easily reuse `/shared` module outside this project without `/data`
3. **Repository interfaces in data layer**: Some developers expect interfaces in domain layer
4. **Deviates from "pure" Clean Architecture**: May confuse developers familiar with Uncle Bob's pattern

### Mitigations

1. **Domain models remain pure**: Even though `/shared` depends on `/data`, domain models have NO framework dependencies
2. **Interface-based repositories**: Repository implementations still use interfaces, enabling mocking and testing
3. **Clear documentation**: Architecture decision recorded in this ADR and CLAUDE.md
4. **Mapper pattern**: DTOs in `/data` are mapped to domain models in `/shared`, maintaining separation

## When to Reconsider This Decision

Re-evaluate this architectural choice if:

1. **Platform-specific data layers diverge**
   - Example: iOS needs CoreData instead of Room
   - Example: Desktop needs custom SQLite implementation
   - Indicator: Platform-specific data implementations that cannot share common interface

2. **Domain logic needs reuse outside mobile**
   - Example: Backend Kotlin server needs same business logic
   - Example: Publishing domain module as standalone library
   - Indicator: External consumers of domain module

3. **Complex domain requires isolation**
   - Example: Financial calculation engine with zero external dependencies
   - Example: Domain logic becomes very complex with intricate business rules
   - Indicator: Domain logic tests requiring complete isolation from any data layer

4. **Team size and expertise changes**
   - Example: Large team with dedicated architecture specialists
   - Example: Team has extensive Clean Architecture experience
   - Indicator: Complexity overhead becomes manageable relative to team size

## Alternatives Considered

### Alternative 1: Clean Architecture with Dependency Inversion

**Structure**: `data → shared ← composeApp`

**Rejected Because**:
- Adds DI complexity for cross-module wiring
- Provides no benefit when all platforms use same data libraries (Room/Ktor KMP)
- Increases development time without measurable quality improvement
- Not recommended by Android's official architecture guide

### Alternative 2: Single Module Monolith

**Structure**: All code in one module

**Rejected Because**:
- Loses multiplatform module separation benefits
- iOS framework generation requires separate module
- No clear architectural boundaries
- Harder to maintain as project grows

### Alternative 3: Hybrid Approach (Repository Interfaces in Both Layers)

**Structure**: Repository interfaces duplicated in `/shared` and `/data`

**Rejected Because**:
- Violates DRY principle
- Maintenance burden (keeping interfaces in sync)
- Confusing for developers (which interface to use?)
- No clear benefit over single-location approach

## Implementation Guidelines

### 1. Module Dependency Configuration

**`/shared/build.gradle.kts`**:
```kotlin
sourceSets {
    commonMain.dependencies {
        implementation(projects.data) // ✅ Domain depends on Data
    }
}
```

**`/data/build.gradle.kts`**:
```kotlin
sourceSets {
    commonMain.dependencies {
        // NO dependency on other modules (leaf node)
        implementation(libs.ktor.client.core)
        implementation(libs.room.runtime)
    }
}
```

**`/composeApp/build.gradle.kts`**:
```kotlin
sourceSets {
    commonMain.dependencies {
        implementation(projects.shared) // ✅ UI depends on Domain
    }
}
```

### 2. Package Structure

#### Data Layer (`/data`)
```
com.example.data/
├── repository/
│   ├── UserRepository.kt           # Interface + Implementation
│   └── ProductRepository.kt
├── database/
│   ├── AppDatabase.kt
│   ├── dao/
│   └── entity/                     # Room entities (DB models)
├── network/
│   ├── ApiClient.kt
│   └── dto/                        # Data Transfer Objects
└── mapper/
    └── UserMapper.kt               # DTO ↔ Domain model
```

#### Domain Layer (`/shared`)
```
org.example.project.judowine/
├── domain/
│   ├── model/                      # Pure Kotlin domain models
│   │   ├── User.kt
│   │   ├── UserId.kt (Value Object)
│   │   └── Email.kt (Value Object)
│   ├── usecase/
│   │   └── GetUserProfileUseCase.kt
│   └── service/
│       └── ValidationService.kt
└── platform/
    └── Platform.kt                 # expect/actual abstractions
```

#### Presentation Layer (`/composeApp`)
```
org.example.project.judowine/
└── ui/
    ├── screen/
    │   └── user/
    │       ├── UserScreen.kt
    │       ├── UserViewModel.kt
    │       └── UserUiState.kt
    ├── component/
    └── navigation/
```

### 3. Dependency Injection Strategy

Use **simple factory pattern** instead of complex DI framework:

```kotlin
// In /composeApp/commonMain
object RepositoryProvider {
    private val database by lazy { AppDatabase.create() }
    private val apiClient by lazy { ApiClient.create() }

    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(database.userDao(), apiClient)
    }
}

// Usage in ViewModel
class UserViewModel(
    private val getUserUseCase: GetUserUseCase = GetUserUseCase(
        RepositoryProvider.userRepository
    )
) : ViewModel() { ... }
```

**For larger projects**, consider lightweight DI (Koin) only if factory pattern becomes unwieldy.

### 4. Testing Strategy

**Domain Logic Tests** (`/shared/commonTest`):
```kotlin
class GetUserProfileUseCaseTest {
    @Test
    fun `should format user profile correctly`() = runTest {
        val fakeRepo = FakeUserRepository() // Mock from test
        val useCase = GetUserProfileUseCase(fakeRepo)

        val result = useCase("user123")

        assertIs<Result.Success>(result)
    }
}
```

**Data Layer Tests** (`/data/commonTest`):
```kotlin
class UserRepositoryImplTest {
    @Test
    fun `should cache user data locally`() = runTest {
        val mockDao = MockUserDao()
        val mockApi = MockApiClient()
        val repo = UserRepositoryImpl(mockDao, mockApi)

        repo.getUser("user123")

        verify(mockDao).insertUser(any())
    }
}
```

## Related Decisions

- [ADR-002: Repository Pattern Implementation](./ADR-002-repository-pattern.md) (Future)
- [ADR-003: Dependency Injection Strategy](./ADR-003-dependency-injection.md) (Future)

## References

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Kotlin Multiplatform Best Practices](https://kotlinlang.org/docs/multiplatform-mobile-samples.html)
- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [The Clean Architecture (Critical Analysis for KMP)](https://proandroiddev.com/clean-architecture-on-android-my-opinion-fb7a6a6a8be)

## Approval

- **Proposed by**: Development Team
- **Reviewed by**: Tech Lead (tech-lead-architect agent)
- **Approved by**: Project Stakeholders
- **Date**: 2025-10-07

---

**Note**: This decision is not set in stone. It should be revisited if project requirements change significantly (see "When to Reconsider This Decision" section).
