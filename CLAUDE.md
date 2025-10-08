# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin Multiplatform project targeting Android, iOS, and Desktop (JVM) platforms using Compose Multiplatform for shared UI. The project demonstrates a multiplatform "Hello World" application with shared business logic and platform-specific implementations.

## Common Development Commands

### Building and Running

**Android Application:**
```bash
./gradlew :composeApp:assembleDebug
```

**Desktop (JVM) Application:**
```bash
./gradlew :composeApp:run
```

**iOS Application:**
Open the `/iosApp` directory in Xcode and run from there, or use the IDE run configuration.

### Testing
```bash
./gradlew test
```

### Clean Build
```bash
./gradlew clean
```

## Project Architecture

### Architectural Pattern: Android UDF (Unidirectional Data Flow)

This project follows **Android's official architecture guidelines** with unidirectional data flow pattern, optimized for Kotlin Multiplatform:

**Dependency Flow:**
```
composeApp → shared → data
```

**Key Principles:**
1. **Unidirectional Data Flow**: Data flows upward (data → domain → UI), events flow downward (UI → domain → data)
2. **Pragmatic over Pure**: Prioritizes developer productivity and maintainability over dogmatic Clean Architecture
3. **KMP-Native Libraries**: All data dependencies (Room, Ktor) are multiplatform, enabling shared data layer across platforms
4. **Repository Pattern**: Both interfaces and implementations reside in `/data` module for simplicity
5. **STRICT LAYER ISOLATION**: `/composeApp` (Presentation) MUST NOT directly import or use ANY classes from `/data` module. All data access MUST go through `/shared` (Domain layer) via Use Cases

**Why Not Clean Architecture?**
- Clean Architecture requires `data → shared` dependency inversion
- This adds complexity (DI frameworks, cross-module interface wiring) without significant benefit for KMP projects using multiplatform data libraries
- Android's official guide recommends `shared → data` for pragmatic, maintainable applications
- See `/docs/architecture/ADR-001-architecture-pattern-selection.md` for detailed analysis

### Module Structure

- **`/composeApp`** - Presentation Layer (UI)
  - `commonMain` - Shared UI code using Compose Multiplatform
  - `commonTest` - Shared UI tests
  - `androidMain` - Android-specific entry point (MainActivity)
  - `jvmMain` - Desktop entry point (MainKt)
  - Package: `org.example.project.judowine`
  - **Depends on**: `/shared` module ONLY
  - **MUST NOT depend on**: `/data` module (direct access forbidden)
  - **Responsibilities**: UI components, ViewModels, navigation, user interaction
  - **Data Access**: ONLY through Use Cases from `/shared` module

- **`/shared`** - Domain Layer (Business Logic)
  - `commonMain` - Platform-agnostic business logic
  - `commonTest` - Shared business logic tests
  - `androidMain/iosMain/jvmMain` - Platform-specific implementations
  - Package: `org.example.project.judowine`
  - **Depends on**: `/data` module
  - **Responsibilities**: Domain models (pure Kotlin), Use Cases, business rules, domain services
  - **Key Point**: Domain models are pure Kotlin with NO framework dependencies

- **`/data`** - Data Layer (Data Sources & Repositories)
  - `commonMain` - Platform-agnostic data layer with Ktor HTTP client and Room database
  - `androidMain` - Android-specific data implementations (Ktor Android engine)
  - `iosMain` - iOS-specific data implementations (Ktor Darwin engine)
  - `jvmMain` - JVM-specific data implementations (Ktor OkHttp engine)
  - `androidHostTest` - Android host tests
  - `androidDeviceTest` - Android device/instrumentation tests
  - Package: `com.example.data`
  - Framework name (iOS): `dataKit`
  - **Depends on**: NO other modules (leaf node)
  - **Responsibilities**: Repository interfaces & implementations, Room DAOs, Ktor clients, DTOs, data source abstractions

- **`/iosApp`** - iOS native application wrapper
  - Contains SwiftUI views that consume the shared Kotlin framework
  - Imports the `Shared` framework generated from the shared module

### Recommended Package Structure

#### `/data` Module (Data Layer)
```
com.example.data/
├── repository/
│   ├── UserRepository.kt           # Interface definition
│   └── UserRepositoryImpl.kt       # Implementation
├── database/
│   ├── AppDatabase.kt              # Room database
│   ├── dao/
│   │   └── UserDao.kt              # Room DAO
│   └── entity/
│       └── UserEntity.kt           # Room entity (DB model)
├── network/
│   ├── ApiClient.kt                # Ktor client
│   └── dto/
│       └── UserDto.kt              # Data Transfer Object
└── mapper/
    └── UserMapper.kt               # DTO ↔ Domain model conversion
```

#### `/shared` Module (Domain Layer)
```
org.example.project.judowine/
├── domain/
│   ├── model/
│   │   ├── User.kt                 # Domain model (pure Kotlin)
│   │   ├── UserId.kt               # Value Object
│   │   └── Email.kt                # Value Object
│   ├── usecase/
│   │   ├── GetUserUseCase.kt       # Business logic orchestration
│   │   └── UpdateUserUseCase.kt
│   └── service/
│       └── UserValidationService.kt # Domain service
└── platform/
    └── Platform.kt                 # Platform abstractions (expect/actual)
```

#### `/composeApp` Module (Presentation Layer)
```
org.example.project.judowine/
└── ui/
    ├── screen/
    │   ├── user/
    │   │   ├── UserScreen.kt       # Composable screen
    │   │   ├── UserViewModel.kt    # State management
    │   │   └── UserUiState.kt      # UI state model
    ├── component/
    │   ├── atom/                   # Atomic Design - Atoms
    │   ├── molecule/               # Atomic Design - Molecules
    │   └── organism/               # Atomic Design - Organisms
    ├── theme/
    │   └── Theme.kt
    └── navigation/
        └── AppNavigation.kt
```

### Key Platform Abstraction Pattern

The project uses Kotlin Multiplatform's `expect/actual` pattern for platform-specific implementations:

- `Platform.kt` (commonMain) - Defines expected platform interface
- `Platform.android.kt`, `Platform.ios.kt`, `Platform.jvm.kt` - Actual platform implementations
- `Greeting.kt` - Business logic that uses platform abstractions

### Technology Stack

- **Kotlin**: 2.2.20
- **Compose Multiplatform**: 1.9.0
- **Android Gradle Plugin**: 8.11.2
- **Target Platforms**:
  - Android (composeApp: API 24+, data: API 29+)
  - iOS (ARM64, X64, Simulator ARM64)
  - Desktop JVM
- **Java Version**: 11 (source/target compatibility)
- **Data Layer Dependencies**:
  - Ktor Client (with platform-specific engines)
  - Room Database (with KSP code generation)
  - kotlinx-datetime

### Build Configuration

- Uses Gradle version catalogs (`libs.versions.toml`) for dependency management
- Kotlin multiplatform plugin with Android, iOS, and JVM targets
- iOS targets: `iosArm64()`, `iosX64()`, `iosSimulatorArm64()`
- Shared module builds as static framework for iOS integration
- Data module builds as `dataKit` framework for iOS
- Room database schema generation in `data/schemas/`
- KSP (Kotlin Symbol Processing) for Room compiler integration

## Agent Usage Guidelines

This project uses specialized agents to handle different architectural concerns. The following guidelines define when and how to invoke each agent.

### Available Agents

1. **strategic-ddd-domain-architect** - Strategic domain modeling and bounded context design
2. **tactical-ddd-shared-implementer** - Domain logic implementation in shared module
3. **tech-lead-architect** - Cross-platform architecture and technical decisions
4. **data-layer-architect** - Data layer design (Room, Ktor, repositories)
5. **compose-ui-architect** - UI implementation with Atomic Design principles
6. **codebase-knowledge-manager** - Automated knowledge extraction after code changes

### Automatic Judgment Flow

#### 1. Initial Judgment (on prompt received)

**Trigger: New domain concept introduced**
- Keywords: "new feature", "business rule", "domain model", "event storming"
- Agent: `strategic-ddd-domain-architect`
- Actions: Define bounded contexts, ubiquitous language, domain events

**Trigger: Architectural decision needed**
- Keywords: "architecture", "module structure", "cross-platform", "expect/actual"
- Affects: Multiple modules or platform-specific concerns
- Agent: `tech-lead-architect`
- Actions: Evaluate architecture, design module interactions, review patterns

**Trigger: Single layer/module change**
- Proceed to Layer Judgment (step 2)

#### 2. Layer Judgment (determine affected components)

**Trigger: `/composeApp` module changes**
- Keywords: "UI", "screen", "Composable", "design", "component"
- Agent: `compose-ui-architect`
- Actions: Design UI components, apply Atomic Design, ensure stateless design

**Trigger: `/shared` module changes**
- Keywords: "business logic", "entity", "value object", "aggregate", "domain service"
- Agent: `tactical-ddd-shared-implementer`
- Actions: Implement tactical DDD patterns, create domain models

**Trigger: `/data` module changes**
- Keywords: "database", "API", "repository", "Room", "Ktor", "network"
- Agent: `data-layer-architect`
- Actions: Design data layer, implement Room DAOs, create Ktor clients

**Trigger: Multiple modules affected**
- Agent: `tech-lead-architect` (coordinator)
- Actions: Coordinate changes across modules, ensure consistency

#### 3. Post-Implementation (mandatory)

**Trigger: Code changes completed**
- Always run after implementation tasks
- Agent: `codebase-knowledge-manager`
- Actions: Extract patterns, document conventions, update implicit knowledge

### Parallel Execution Cases

**Case 1: Data Layer + UI Layer simultaneous implementation**
```
Run in parallel:
  - data-layer-architect (data layer implementation)
  - compose-ui-architect (UI layer implementation)
```

**Case 2: Full-stack feature requiring complete design**
```
Sequential:
  1. strategic-ddd-domain-architect (domain design)
  2. tech-lead-architect (architecture design)

Parallel:
  3. tactical-ddd-shared-implementer (business logic)
  4. data-layer-architect (data layer)
  5. compose-ui-architect (UI layer)

Sequential:
  6. codebase-knowledge-manager (knowledge extraction)
```

**Case 3: Platform-specific implementation**
```
Sequential:
  1. tech-lead-architect (evaluate expect/actual pattern)

Parallel:
  2. Relevant platform agent (androidMain/iosMain/jvmMain)

Sequential:
  3. codebase-knowledge-manager (knowledge extraction)
```

### Agent Collaboration Patterns

#### Pattern A: New Feature Development (Full-Stack)

**When to use**: Adding a complete new feature that spans all layers

**Flow**:
```
1. strategic-ddd-domain-architect
   └─> Domain modeling, event storming, bounded context definition

2. tech-lead-architect
   └─> Architecture design, module interaction, platform concerns

3. [PARALLEL EXECUTION]
   ├─> tactical-ddd-shared-implementer (shared business logic)
   ├─> data-layer-architect (Room entities, DAOs, Ktor clients, repositories)
   └─> compose-ui-architect (UI screens, components, state management)

4. codebase-knowledge-manager
   └─> Extract patterns, document decisions
```

**Example**: "Add user authentication with login screen, credential storage, and API integration"

#### Pattern B: Single Layer Modification

**When to use**: Changes confined to one architectural layer

**Flow**:
```
1. Relevant layer agent (design + implementation)
   └─> compose-ui-architect OR tactical-ddd-shared-implementer OR data-layer-architect

2. codebase-knowledge-manager
   └─> Extract patterns, document changes
```

**Examples**:
- "Refactor the login screen UI" → `compose-ui-architect`
- "Add validation logic to User entity" → `tactical-ddd-shared-implementer`
- "Optimize database queries in UserRepository" → `data-layer-architect`

#### Pattern C: Architectural Change

**When to use**: Changes that impact overall architecture or multiple modules

**Flow**:
```
1. tech-lead-architect
   └─> Impact analysis, design changes, migration strategy

2. [SEQUENTIAL EXECUTION] Affected layer agents
   ├─> First: tactical-ddd-shared-implementer (if shared affected)
   ├─> Then: data-layer-architect (if data affected)
   └─> Finally: compose-ui-architect (if UI affected)

3. codebase-knowledge-manager
   └─> Extract patterns, document architectural decisions
```

**Examples**:
- "Refactor Platform abstraction pattern"
- "Add new target platform (e.g., Web)"
- "Restructure module dependencies"

### Agent Trigger Conditions Summary

| Condition | Primary Agent | Secondary Agent(s) |
|-----------|---------------|-------------------|
| New business feature | strategic-ddd-domain-architect | tech-lead-architect |
| Architectural decision | tech-lead-architect | All (for review) |
| UI implementation/change | compose-ui-architect | tech-lead-architect (review) |
| Business logic in `/shared` | tactical-ddd-shared-implementer | strategic-ddd-domain-architect (validation) |
| Data layer in `/data` | data-layer-architect | tech-lead-architect (review) |
| Cross-platform concern | tech-lead-architect | Platform-specific agents |
| Multiple module changes | tech-lead-architect | Affected layer agents |
| Refactoring | tech-lead-architect + layer agent | codebase-knowledge-manager |
| Bug fix | Affected layer agent | - |
| Any code completion | codebase-knowledge-manager | - (auto-run) |

### Questions and Confirmations

When the user asks questions or seeks confirmation rather than requesting implementation, use the following agent selection criteria:

#### Question Type → Agent Mapping

**1. Architecture & Design Questions**
- Examples:
  - "Is this module structure appropriate?"
  - "How should I use the expect/actual pattern?"
  - "Should this be in shared or data module?"
  - "What's the best way to structure cross-platform code?"
- **Agent**: `tech-lead-architect`
- **Rationale**: Requires deep understanding of overall project architecture

**2. Domain Modeling & Business Logic Questions**

*Strategic Level (Domain Design)*
- Examples:
  - "How should I define bounded contexts for this feature?"
  - "What's the ubiquitous language for this domain?"
  - "Should these be separate aggregates?"
- **Agent**: `strategic-ddd-domain-architect`
- **Rationale**: Requires strategic domain-driven design expertise

*Tactical Level (Implementation Patterns)*
- Examples:
  - "Should this be an Entity or Value Object?"
  - "How do I implement this domain service?"
  - "What's the correct aggregate boundary?"
- **Agent**: `tactical-ddd-shared-implementer`
- **Rationale**: Requires tactical DDD pattern knowledge

**3. Data Layer Questions**
- Examples:
  - "How should I handle Room database migration?"
  - "Is this API design appropriate?"
  - "How do I structure this repository?"
  - "What's the best Ktor client configuration?"
- **Agent**: `data-layer-architect`
- **Rationale**: Requires expertise in data persistence and networking

**4. UI Implementation Questions**
- Examples:
  - "How should I design this component?"
  - "Which Atomic Design level does this belong to?"
  - "Should this be stateful or stateless?"
  - "How do I make this component reusable?"
- **Agent**: `compose-ui-architect`
- **Rationale**: Requires UI design patterns and Compose expertise

**5. Codebase Understanding & Exploration**
- Examples:
  - "Where is feature X implemented in this project?"
  - "What patterns are currently used for Y?"
  - "How does the existing code handle Z?"
  - "What conventions does this codebase follow?"
- **Agent**: `codebase-knowledge-manager`
- **Rationale**: Specializes in extracting and analyzing existing code patterns

**6. Simple Questions & Clarifications**
- Examples:
  - "What's in this file?"
  - "What does this error mean?"
  - "How do I add a dependency?"
  - "What version of Kotlin is used?"
  - "Can you explain this code snippet?"
- **Agent**: None (Main assistant handles directly)
- **Rationale**: Simple questions don't require specialized agent expertise

#### Decision Tree for Questions

```
Is it a question/confirmation?
  │
  ├─> YES → Determine question type:
  │         │
  │         ├─> Architecture/Cross-platform → tech-lead-architect
  │         ├─> Strategic domain design → strategic-ddd-domain-architect
  │         ├─> Tactical DDD patterns → tactical-ddd-shared-implementer
  │         ├─> Data layer (Room/Ktor) → data-layer-architect
  │         ├─> UI design/components → compose-ui-architect
  │         ├─> Existing code patterns → codebase-knowledge-manager
  │         └─> Simple/General → Main assistant (no agent)
  │
  └─> NO → Follow implementation workflow (see Automatic Judgment Flow)
```

### Best Practices

1. **Always start with domain understanding** - Use `strategic-ddd-domain-architect` for new features before implementation
2. **Run agents in parallel when possible** - Independent tasks can be executed concurrently
3. **Always run knowledge manager** - `codebase-knowledge-manager` must run after code changes
4. **Coordinate with tech-lead** - For multi-module changes, involve `tech-lead-architect` first
5. **Layer isolation** - Single-layer changes should use the specific layer agent directly
6. **Question before implementation** - When unsure, ask the appropriate specialized agent first

## Project Orchestration

For large-scale projects with multiple units and tasks (e.g., projects defined in `docs/inception/*.md`), use the `project-orchestrator` agent to systematically manage implementation:

### When to Use Project Orchestrator

- **Starting a new project**: When implementing from inception documents (e.g., `docs/inception/event_meet.md`)
- **Complex multi-unit features**: When work spans many sequential units with dependencies
- **Quality-controlled development**: When mandatory review cycles are required for every task

### Project Orchestrator Workflow

The `project-orchestrator` manages implementation with strict quality controls:

1. **Progress Tracking**
   - Creates `docs/progress/project-status.md` to track all units and tasks
   - Updates progress after every task completion
   - Maintains complete audit trail with timestamps

2. **Sequential Unit Execution**
   - Processes units one-by-one (no parallel unit execution)
   - Verifies dependencies before starting each unit
   - Ensures builds pass before proceeding

3. **Mandatory Review Cycle (for every task)**
   ```
   Implementation → Build Verification → Parallel Reviews → Fix Decision → Task Complete
                                        ↓
                         (codebase-knowledge-manager + tech-lead-architect)
   ```

4. **Review Documentation**
   - Creates `docs/reviews/unit-{N}-{task-name}.md` for every task
   - Implementation agent makes fix/accept decisions
   - All decisions are documented with justification

5. **Final Report**
   - Generates `docs/reports/project-completion-report.md` when all units complete
   - Includes agent contributions, quality metrics, and lessons learned

### Example Usage

```
User: "Start implementing the EventMeet project based on docs/inception/event_meet.md"
Assistant: Uses project-orchestrator to:
  1. Parse 11 units from event_meet.md
  2. Create progress tracking file
  3. Sequentially implement Unit-1 through Unit-11
  4. Review every task with mandatory quality checks
  5. Generate final completion report
```

### Quality Guarantees

When using `project-orchestrator`:
- ✅ 100% review coverage - every task is reviewed
- ✅ 100% build success - no broken builds progress
- ✅ Complete audit trail - all decisions documented
- ✅ Dependency enforcement - correct execution order
- ✅ Agent accountability - clear attribution for all work