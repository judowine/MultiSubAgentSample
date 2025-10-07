# EventMeet - Product Backlog Items (PBIs)

## Overview

This document defines the Product Backlog Items (PBIs) for the EventMeet project. Each PBI represents a vertically-sliced, independently valuable feature that can be delivered incrementally.

**Total PBIs**: 7
**Estimated Duration**: 4 Phases

---

## PBI Prioritization Summary

| Priority | PBI ID | PBI Title | Complexity | User Value | Dependencies |
|----------|--------|-----------|------------|------------|--------------|
| 1 | PBI-1 | User Profile Management Foundation | Medium | High | None |
| 2 | PBI-2 | Event Discovery & Viewing | Large | High | PBI-1 |
| 3 | PBI-3 | People Search & Discovery | Medium | Medium | None (parallel with PBI-2) |
| 4 | PBI-4 | Meeting Record Creation | Large | Critical | PBI-2, PBI-3 |
| 5 | PBI-5 | Meeting Notes & Tagging | Medium | High | PBI-4 |
| 6 | PBI-6 | People-Centric Meeting History | Medium | High | PBI-5 |
| 7 | PBI-7 | Event-Centric Meeting Review | Small | Medium | PBI-2, PBI-5 |

---

## PBI Definitions

### PBI-1: User Profile Management Foundation
**Priority**: 1
**Complexity**: Medium
**Related Units**: Unit-1 (partial), Unit-3
**Related Epics**: Epic-1 (User Profile Management)
**Related User Stories**: US-1 (Self Event Viewing)

#### User Value Statement
**As a** connpass user
**I want to** register and manage my connpass profile in the app
**So that** I can connect my connpass identity and start tracking my event participation

#### Description
Implement the foundational user profile management system, including local database setup for user data and a complete profile registration/editing flow. This PBI establishes the core identity layer needed for all subsequent features.

#### Scope
**Included:**
- Room database setup with user profile entity
- User profile registration screen (connpass ID/nickname input)
- User profile display screen
- User profile editing capability
- Local data persistence

**Excluded:**
- Event entities and DAOs (deferred to PBI-2)
- Meeting record entities (deferred to PBI-4)

#### Technical Implementation (Unit Mapping)
- **Unit-1 (partial)**: Room setup + User profile entity + User DAO
- **Unit-3 (complete)**: Profile registration/display/editing screens

#### Acceptance Criteria
- [ ] User can register a connpass ID and nickname
- [ ] User profile is persisted to Room database
- [ ] User can view their registered profile
- [ ] User can edit their profile information
- [ ] Profile data survives app restart
- [ ] Input validation prevents empty ID/nickname
- [ ] Build passes with `./gradlew build`

#### Dependencies
- None (can start immediately)

---

### PBI-2: Event Discovery & Viewing
**Priority**: 2
**Complexity**: Large
**Related Units**: Unit-1 (partial), Unit-2, Unit-4
**Related Epics**: Epic-2 (Event Management)
**Related User Stories**: US-1 (Self Event Viewing)

#### User Value Statement
**As a** connpass user
**I want to** view my participated events fetched from connpass API
**So that** I can review my event history and select events for meeting records

#### Description
Implement the complete event discovery system, including connpass API integration, event data persistence, and event listing UI. This PBI delivers the first "real data" feature by connecting to external API.

#### Scope
**Included:**
- connpass API client setup (Ktor)
- Event entity and DAO (Room)
- connpass API event endpoints integration
- Event DTOs and mappers
- Event list screen (sorted by date)
- Event detail screen
- Error handling for API failures

**Excluded:**
- Meeting records association (deferred to PBI-4)
- Event-specific meeting display (deferred to PBI-7)

#### Technical Implementation (Unit Mapping)
- **Unit-1 (partial)**: Event entity + Event DAO
- **Unit-2 (complete)**: Ktor setup + connpass API client + DTOs + error handling
- **Unit-4 (complete)**: Event list screen + API integration + detail screen

#### Acceptance Criteria
- [ ] User can view list of their participated events from connpass API
- [ ] Events are displayed in chronological order (newest first)
- [ ] Event list shows: title, date, location (summary info)
- [ ] User can tap an event to view detailed information
- [ ] Event detail shows: full description, organizer, participants count
- [ ] Events are cached in local Room database
- [ ] Offline mode: cached events display when API unavailable
- [ ] Error states are handled gracefully (network error, API error)
- [ ] Pull-to-refresh functionality updates event list
- [ ] Build passes with `./gradlew build`

#### Dependencies
- **PBI-1**: Requires user profile to identify which user's events to fetch

---

### PBI-3: People Search & Discovery
**Priority**: 3
**Complexity**: Medium
**Related Units**: Unit-2 (reuse), Unit-5, Unit-6
**Related Epics**: Epic-3 (User Search & Management)
**Related User Stories**: US-6 (User Activity Viewing)

#### User Value Statement
**As a** user
**I want to** search for other connpass users and view their event participation
**So that** I can discover people I've met and see their interests/activity

#### Description
Implement user search functionality with connpass API, allowing users to find other participants by nickname/ID and view their event history. This PBI enables social discovery within the connpass ecosystem.

#### Scope
**Included:**
- User search screen with search input
- connpass user search API integration
- Search results list display
- User detail screen
- User's participated events display
- Common events detection (overlap with logged-in user)

**Excluded:**
- Meeting record creation from search (deferred to PBI-4)
- User profile persistence to local DB (search is API-only)

#### Technical Implementation (Unit Mapping)
- **Unit-2 (reuse)**: Extend Ktor client with user search endpoints
- **Unit-5 (complete)**: User search screen + search API integration + results display
- **Unit-6 (complete)**: User detail screen + user events API + common events logic

#### Acceptance Criteria
- [ ] User can search for connpass users by nickname or ID
- [ ] Search results display user nickname and ID
- [ ] User can tap a search result to view user details
- [ ] User detail screen shows: nickname, ID, profile info (if available)
- [ ] User detail screen displays list of their participated events
- [ ] Common events between searched user and logged-in user are highlighted
- [ ] Empty search results show appropriate message
- [ ] Search handles API errors gracefully
- [ ] Build passes with `./gradlew build`

#### Dependencies
- **None** (can be developed in parallel with PBI-2)
- **Soft dependency**: Benefits from PBI-1 for "common events" feature, but can work standalone

---

### PBI-4: Meeting Record Creation
**Priority**: 4
**Complexity**: Large
**Related Units**: Unit-1 (partial), Unit-7
**Related Epics**: Epic-4 (Meeting Record Management)
**Related User Stories**: US-2 (Record People Met at Events)

#### User Value Statement
**As a** event participant
**I want to** record who I met at specific events
**So that** I can remember connections and avoid "who was that person?" moments

#### Description
Implement the core meeting record functionality, allowing users to create associations between events and people they met. This PBI delivers the primary value proposition of the app.

#### Scope
**Included:**
- Meeting record entity and DAO (Room)
- Meeting record creation screen
- Event selection flow (from event list)
- User selection flow (from search)
- Meeting record persistence
- Basic meeting record listing (without notes/tags)

**Excluded:**
- Notes and tags (deferred to PBI-5)
- Meeting record detail editing (deferred to PBI-5)
- Advanced filtering/grouping (deferred to PBI-6, PBI-7)

#### Technical Implementation (Unit Mapping)
- **Unit-1 (partial)**: Meeting record entity + Meeting record DAO
- **Unit-7 (complete)**: Meeting record creation screen + event/user selection + DB save

#### Acceptance Criteria
- [ ] User can create a meeting record from an event detail screen
- [ ] User can select "add person met" action
- [ ] User can search and select a connpass user to associate
- [ ] Meeting record (event ID + user ID + timestamp) is saved to Room DB
- [ ] User can view list of people they've met (basic list)
- [ ] Meeting records persist across app restarts
- [ ] Duplicate meeting records (same event + user) are prevented
- [ ] Build passes with `./gradlew build`

#### Dependencies
- **PBI-2**: Requires events to exist for selection
- **PBI-3**: Requires user search to find people

---

### PBI-5: Meeting Notes & Tagging
**Priority**: 5
**Complexity**: Medium
**Related Units**: Unit-1 (partial), Unit-8, Unit-9
**Related Epics**: Epic-4 (Meeting Record Management)
**Related User Stories**: US-3 (Memo Conversation Content), US-4 (Review People Met)

#### User Value Statement
**As a** user
**I want to** add notes and tags to meeting records
**So that** I can remember what we discussed and categorize conversations for later reference

#### Description
Enhance meeting records with rich metadata (notes and tags), enabling users to capture conversation context and categorize meetings. This PBI transforms simple records into actionable memory aids.

#### Scope
**Included:**
- Tag entity and DAO (Room)
- Meeting record note field (text memo)
- Tag input UI with autocomplete
- Tag suggestion from previously used tags
- New tag creation capability
- Meeting record detail screen with notes/tags display
- Meeting record editing capability
- Meeting record deletion

**Excluded:**
- Advanced tag filtering across all records (deferred to PBI-6)

#### Technical Implementation (Unit Mapping)
- **Unit-1 (partial)**: Tag entity + Tag DAO + Meeting record note field migration
- **Unit-8 (complete)**: Note input UI + tag input UI + tag suggestion + save logic
- **Unit-9 (complete)**: Meeting record list screen + detail screen + edit/delete

#### Acceptance Criteria
- [ ] User can add a text note to a meeting record
- [ ] User can add tags to a meeting record (multiple tags allowed)
- [ ] Tag input shows suggestions from previously used tags
- [ ] User can create new tags on-the-fly
- [ ] User can view meeting record details with notes and tags
- [ ] User can edit existing meeting records (notes and tags)
- [ ] User can delete meeting records
- [ ] Tags are reusable across multiple meeting records
- [ ] Meeting record list displays note preview and tags
- [ ] Build passes with `./gradlew build`

#### Dependencies
- **PBI-4**: Requires meeting records to exist for enhancement

---

### PBI-6: People-Centric Meeting History
**Priority**: 6
**Complexity**: Medium
**Related Units**: Unit-10
**Related Epics**: Epic-5 (History & Review)
**Related User Stories**: US-4 (Review People Met)

#### User Value Statement
**As a** user
**I want to** view all meetings grouped by person
**So that** I can review my history with specific individuals before meeting them again

#### Description
Implement people-centric view that aggregates all meeting records by person, showing complete interaction history. This PBI enables users to prepare for future encounters by reviewing past conversations.

#### Scope
**Included:**
- People list screen (all people user has met)
- Person detail screen
- Meeting history timeline for each person (chronological)
- Tag-based filtering on person's meeting history
- Quick access to related events

**Excluded:**
- Event-centric view (deferred to PBI-7)

#### Technical Implementation (Unit Mapping)
- **Unit-10 (complete)**: People list screen + person detail screen + meeting history display + tag filtering

#### Acceptance Criteria
- [ ] User can view a list of all people they've met
- [ ] People list shows: name, number of meetings, most recent meeting date
- [ ] User can tap a person to view their detail page
- [ ] Person detail shows all meeting records with that person
- [ ] Meeting records are displayed chronologically
- [ ] Each meeting record shows: event name, date, notes, tags
- [ ] User can filter person's meetings by tag
- [ ] User can navigate from meeting record to event detail
- [ ] Build passes with `./gradlew build`

#### Dependencies
- **PBI-5**: Requires notes and tags for meaningful history display

---

### PBI-7: Event-Centric Meeting Review
**Priority**: 7
**Complexity**: Small
**Related Units**: Unit-11
**Related Epics**: Epic-5 (History & Review)
**Related User Stories**: US-5 (Event Meeting Review)

#### User Value Statement
**As a** user
**I want to** view all people I met at a specific event
**So that** I can review event-specific networking outcomes

#### Description
Extend event detail screen to show all people met at that event, providing event-centric view of meeting records. This PBI completes the dual-axis review capability (people-centric + event-centric).

#### Scope
**Included:**
- Event detail screen enhancement
- "People met at this event" section
- Meeting record summaries (with notes/tags)
- Navigation from event to meeting record details

**Excluded:**
- None (final feature PBI)

#### Technical Implementation (Unit Mapping)
- **Unit-11 (complete)**: Event detail screen extension + people met list + meeting record navigation

#### Acceptance Criteria
- [ ] Event detail screen shows "People met at this event" section
- [ ] Section displays all meeting records for that event
- [ ] Each meeting record shows: person name, note preview, tags
- [ ] User can tap a meeting record to view full details
- [ ] Empty state shown if no one met at event yet
- [ ] User can add new meeting record from event detail
- [ ] Build passes with `./gradlew build`

#### Dependencies
- **PBI-2**: Requires event detail screen foundation
- **PBI-5**: Requires meeting records with notes/tags

---

## PBI-to-Unit Mapping Matrix

| PBI | Unit-1 | Unit-2 | Unit-3 | Unit-4 | Unit-5 | Unit-6 | Unit-7 | Unit-8 | Unit-9 | Unit-10 | Unit-11 |
|-----|--------|--------|--------|--------|--------|--------|--------|--------|--------|---------|---------|
| PBI-1 | User entity | - | ✓ | - | - | - | - | - | - | - | - |
| PBI-2 | Event entity | ✓ | - | ✓ | - | - | - | - | - | - | - |
| PBI-3 | - | Reuse | - | - | ✓ | ✓ | - | - | - | - | - |
| PBI-4 | Meeting entity | - | - | - | - | - | ✓ | - | - | - | - |
| PBI-5 | Tag entity | - | - | - | - | - | - | ✓ | ✓ | - | - |
| PBI-6 | - | - | - | - | - | - | - | - | - | ✓ | - |
| PBI-7 | - | - | - | - | - | - | - | - | - | - | ✓ |

**Note**: Unit-1 (DB foundation) is incrementally built across PBI-1, PBI-2, PBI-4, and PBI-5 as entities are needed.

---

## PBI-to-User Story Mapping

| User Story | Related PBIs | Implementation Coverage |
|------------|-------------|------------------------|
| US-1: Self Event Viewing | PBI-1, PBI-2 | Complete |
| US-2: Record People Met | PBI-4 | Complete |
| US-3: Memo Conversation | PBI-5 | Complete |
| US-4: Review People Met | PBI-5, PBI-6 | Complete |
| US-5: Event Meeting Review | PBI-7 | Complete |
| US-6: User Activity Viewing | PBI-3 | Complete |

---

## PBI-to-Epic Mapping

| Epic | Related PBIs | Coverage |
|------|-------------|----------|
| Epic-1: User Profile Management | PBI-1 | Complete |
| Epic-2: Event Management | PBI-2 | Complete |
| Epic-3: User Search & Management | PBI-3 | Complete |
| Epic-4: Meeting Record Management | PBI-4, PBI-5 | Complete |
| Epic-5: History & Review | PBI-6, PBI-7 | Complete |

---

## Dependency Graph

```
PBI-1 (User Profile)
  └─> PBI-2 (Events) ──┬─> PBI-4 (Meeting Records) ──> PBI-5 (Notes/Tags) ──┬─> PBI-6 (People History)
                       │                                                     │
PBI-3 (User Search) ───┘                                                     └─> PBI-7 (Event History)
```

**Parallel Tracks**:
- Track A: PBI-1 → PBI-2
- Track B: PBI-3 (can run parallel with Track A after infrastructure)
- Convergence: PBI-4 (requires both tracks)
- Linear: PBI-4 → PBI-5 → PBI-6 / PBI-7 (PBI-6 and PBI-7 can be parallel)

---

## Development Phases (Aligned with Original Document)

### Phase 1: Foundation (PBI-1, PBI-2 start, PBI-3 start)
**Goal**: Establish infrastructure and core data flows

**Deliverables**:
- User profile management
- Database foundation (partial)
- API client foundation (partial)

**Value**: Users can register and begin using the app

---

### Phase 2: Data Integration (PBI-2 complete, PBI-3 complete)
**Goal**: Complete external data connectivity

**Deliverables**:
- Full event viewing from connpass API
- User search functionality
- Database foundation for events

**Value**: Users can explore events and find people

---

### Phase 3: Core Recording (PBI-4, PBI-5)
**Goal**: Enable primary app functionality (recording meetings)

**Deliverables**:
- Meeting record creation
- Notes and tags capability
- Full database schema

**Value**: Users can record and annotate meetings (MVP feature)

---

### Phase 4: Review & Insights (PBI-6, PBI-7)
**Goal**: Enable reflection and future preparation

**Deliverables**:
- People-centric history view
- Event-centric history view

**Value**: Users can review past interactions before future meetings

---

## Complexity Estimation Rationale

### Medium Complexity (3 PBIs)
- **PBI-1**: Single entity, basic CRUD, 2 screens
- **PBI-3**: API integration reuse, 2 screens, moderate logic
- **PBI-5**: Entity extension, UI enhancement, tag management
- **PBI-6**: Query-heavy, aggregation logic, 2 screens

### Large Complexity (2 PBIs)
- **PBI-2**: Multi-unit integration (DB + API + UI), error handling, caching
- **PBI-4**: Complex entity relationships, multi-step flow, data integrity

### Small Complexity (2 PBIs)
- **PBI-7**: Screen extension only, reuses existing components

---

## Acceptance Testing Strategy

### Per-PBI Testing
Each PBI includes:
- Unit tests for domain logic and repositories
- Integration tests for API clients (with mocks)
- UI tests for critical user flows (Compose UI Testing)
- Build verification (`./gradlew build` must pass)

### Cross-PBI Integration Testing
After each phase:
- End-to-end user journey testing
- Data consistency verification across PBIs
- Performance testing (especially API + DB interactions)

### Pre-Release Testing
Before final release:
- Full regression suite across all PBIs
- Offline mode testing (API unavailable scenarios)
- Database migration testing (simulated upgrades)

---

## Risk Mitigation

### PBI-2 Risk: connpass API Rate Limiting
- **Mitigation**: Implement aggressive caching, request throttling
- **Contingency**: Add manual event entry fallback

### PBI-4 Risk: Duplicate Meeting Record Prevention
- **Mitigation**: Unique constraint in Room DB (event_id + user_id)
- **Contingency**: UI confirmation dialog for duplicates

### PBI-5 Risk: Tag Management Complexity
- **Mitigation**: Keep tag schema simple (many-to-many relationship)
- **Contingency**: Limit tags per meeting record if performance degrades

---

## Notes for Implementation

1. **Incremental Database Migrations**: Unit-1 entities are added progressively (PBI-1 → PBI-2 → PBI-4 → PBI-5). Each PBI must include proper Room migration strategy.

2. **API Client Reuse**: Unit-2 (Ktor client) established in PBI-2 is reused in PBI-3. Ensure modular design for endpoint additions.

3. **UI Component Reusability**: Follow Atomic Design principles to maximize component reuse (e.g., user search UI in PBI-3 can be embedded in PBI-4).

4. **Testing Data Setup**: Later PBIs (PBI-6, PBI-7) require substantial test data. Consider test data generators for meeting records.

5. **Parallel Development Window**: PBI-2 and PBI-3 can be developed simultaneously after PBI-1 completes, saving ~1 sprint.

6. **MVP Definition**: PBI-1 through PBI-5 constitute the Minimum Viable Product. PBI-6 and PBI-7 are "nice-to-have" enhancements for better UX.

---

## Revision History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 1.0 | 2025-10-08 | Initial PBI breakdown | Project Orchestrator |