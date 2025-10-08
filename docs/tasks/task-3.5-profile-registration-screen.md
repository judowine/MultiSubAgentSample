# Task 3.5: Create ProfileRegistrationScreen.kt

## Context
- **Project**: EventMeet
- **PBI**: PBI-1 - User Profile Management Foundation
- **Current Phase**: Phase 1 - Foundation
- **Task Priority**: Sequential task 3.5 (following completed domain layer tasks 3.1-3.4)

## Architecture Context
- **Module**: /composeApp (Presentation Layer)
- **Package**: org.example.project.judowine.ui.screen.profile
- **Pattern**: MVI (Model-View-Intent) - will be implemented in Task 3.8
- **Design System**: Atomic Design principles

## Available Dependencies

### Domain Layer (/shared)
- **User.kt**: Domain model located at `/shared/src/commonMain/kotlin/org/example/project/judowine/domain/model/User.kt`
  ```kotlin
  data class User(
      val id: Long,
      val connpassId: String,
      val nickname: String,
      val createdAt: Instant,
      val updatedAt: Instant
  )
  ```
- **SaveUserProfileUseCase.kt**: Located at `/shared/src/commonMain/kotlin/org/example/project/judowine/domain/usecase/SaveUserProfileUseCase.kt`

### Data Layer (/data)
- **UserRepository**: Returns UserEntity (data layer type)

## Requirements

### Screen Purpose
Create a registration screen where new users can:
1. Enter their connpass ID
2. Enter their nickname
3. Submit to create a profile

### Acceptance Criteria (from PBI-1)
- User can register a connpass ID and nickname
- Input validation prevents empty ID/nickname
- Profile data is persisted to Room database (via use case)

### Implementation Guidelines

1. **File Location**: `/composeApp/src/commonMain/kotlin/org/example/project/judowine/ui/screen/profile/ProfileRegistrationScreen.kt`

2. **Screen Design**:
   - Title: "Create Your Profile"
   - Input field: Connpass ID (required, non-empty)
   - Input field: Nickname (required, non-empty)
   - Submit button: "Create Profile"
   - Validation: Show error message if fields are empty on submit

3. **State Management**:
   - For now, use local state with `remember` and `mutableStateOf`
   - Task 3.8 will introduce ProfileViewModel with MVI pattern
   - Screen should be designed to easily integrate with ViewModel later

4. **Component Structure (Atomic Design)**:
   - Use basic Compose components for now (TextField, Button, Text)
   - Task 3.9 will create reusable atomic components
   - Design screen to allow easy refactoring to atomic components

5. **Interaction Flow**:
   - User enters connpass ID and nickname
   - On "Create Profile" button click:
     - Validate inputs (non-empty)
     - If valid: Save profile using SaveUserProfileUseCase
     - If invalid: Show error message
   - On success: Navigate to profile display screen (placeholder for now)

6. **Use Case Integration**:
   - Create instance of SaveUserProfileUseCase with UserRepository
   - Call use case on submit with User domain model
   - Handle success/error states
   - Use kotlinx.coroutines for async operations

## Constraints
- Follow Compose Multiplatform best practices
- Design for stateless components where possible
- Keep business logic minimal (will move to ViewModel in Task 3.8)
- Use Material3 design components
- Ensure screen is testable and maintainable
- Must compile and pass `./gradlew build`

## Review Requirements
- This task will be reviewed by:
  - codebase-knowledge-manager (pattern analysis)
  - tech-lead-architect (architectural review)
- Implementation must pass `./gradlew build` before review

## Expected Output
- ProfileRegistrationScreen.kt with:
  - Composable function for the screen
  - Input validation logic
  - SaveUserProfileUseCase integration
  - Clean, maintainable code ready for ViewModel integration
  - Proper error handling and loading states

## Dependencies to Check
- composeApp module already has Compose Multiplatform dependencies
- shared module is already a dependency of composeApp
- kotlinx-datetime and kotlinx-coroutines are available
