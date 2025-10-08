# Task 3.6: Create ProfileDisplayScreen.kt

**Context**: EventMeet project, PBI-1: User Profile Management Foundation

**Requirements**:
Create ProfileDisplayScreen.kt that displays user profile data following the established Screen/Content pattern from ProfileRegistrationScreen.kt (Task 3.5).

**File Location**:
`/Users/shota-kuroda/KotlinMultiplatformProject/MultiSubAgentSample/composeApp/src/commonMain/kotlin/org/example/project/judowine/ui/screen/profile/ProfileDisplayScreen.kt`

**Dependencies**:
- GetUserProfileUseCase from `/Users/shota-kuroda/KotlinMultiplatformProject/MultiSubAgentSample/shared/src/commonMain/kotlin/org/example/project/judowine/domain/usecase/GetUserProfileUseCase.kt`
- UserProfile domain model from shared module

**Features to Implement**:
1. Display user profile information:
   - Connpass ID
   - Nickname
   - Created timestamp
   - Updated timestamp
2. Handle loading states (while fetching profile)
3. Handle error states (user not found, network error, etc.)
4. Follow Material3 design guidelines
5. 100% multiplatform compatible (commonMain)

**Architecture Constraints**:
- Follow Screen/Content component pair pattern (see ProfileRegistrationScreen.kt)
- NO direct data layer access - use GetUserProfileUseCase only
- Stateless presentation components with state hoisting
- Follow Android UDF: composeApp → shared → data

**Expected Pattern**:
```kotlin
@Composable
fun ProfileDisplayScreen(
    userId: UserId,
    getProfileUseCase: GetUserProfileUseCase = remember { ... }
) {
    var state by remember { mutableStateOf<DisplayState>(...) }

    LaunchedEffect(userId) {
        // Load profile using GetUserProfileUseCase
    }

    ProfileDisplayContent(
        state = state,
        onAction = { /* ... */ }
    )
}

@Composable
private fun ProfileDisplayContent(
    state: DisplayState,
    onAction: (DisplayAction) -> Unit
) {
    // Pure presentation
}
```

**State Management**:
- Create sealed interface for DisplayState (Loading, Success, Error)
- Create sealed interface for DisplayAction (if needed for interactions)
- Use LaunchedEffect for loading profile on screen entry

**Quality Checklist**:
- [ ] Follows Screen/Content pattern from Task 3.5
- [ ] NO direct data/repository access
- [ ] Material3 components only
- [ ] 100% commonMain (no platform-specific code)
- [ ] Handles all states (Loading, Success, Error)
- [ ] Stateless presentation components

Please implement ProfileDisplayScreen.kt following these requirements.
