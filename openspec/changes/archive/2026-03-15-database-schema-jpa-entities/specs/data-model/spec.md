## ADDED Requirements

### Requirement: BaseEntity with common audit fields
The system SHALL provide a `BaseEntity` abstract class at `com.fluenz.api.entity.BaseEntity` with fields:
- `id` (UUID, auto-generated)
- `createdAt` (LocalDateTime, set on creation, not updatable)
- `updatedAt` (LocalDateTime, updated on every save)

All other entities SHALL extend `BaseEntity`.

#### Scenario: Audit fields auto-populated
- **WHEN** a new entity is saved to the database
- **THEN** `id` is auto-generated as a UUID, `createdAt` is set to the current time, and `updatedAt` is set to the current time

#### Scenario: UpdatedAt changes on update
- **WHEN** an existing entity is modified and saved
- **THEN** `updatedAt` is updated to the current time, while `createdAt` remains unchanged

---

### Requirement: User entity
The system SHALL provide a `User` entity with fields:
- `email` (String, unique, not null)
- `username` (String, unique, not null)
- `passwordHash` (String, not null)
- `currentLevel` (Level enum: BEGINNER, INTERMEDIATE, ADVANCED)
- `goals` (String, nullable — user's learning goals text)

#### Scenario: User created with required fields
- **WHEN** a user record is created with email, username, and password hash
- **THEN** the record is persisted with a UUID and audit timestamps

#### Scenario: Email uniqueness enforced
- **WHEN** a user record is created with a duplicate email
- **THEN** the database rejects the insert with a constraint violation

---

### Requirement: Profession entity
The system SHALL provide a `Profession` entity with fields:
- `name` (String, unique, not null — e.g., "Software Engineer", "Marketing", "F&B")
- `description` (String, nullable)

#### Scenario: Profession stored
- **WHEN** a profession record is created with a name
- **THEN** the record is persisted and queryable by name

---

### Requirement: LearningPath entity
The system SHALL provide a `LearningPath` entity with fields:
- `title` (String, not null — AI-generated path title)
- `status` (PathStatus enum: ACTIVE, ARCHIVED)
- `user` (ManyToOne relationship to User)
- `profession` (ManyToOne relationship to Profession)
- `situations` (OneToMany relationship to Situation, cascade ALL)

#### Scenario: User has multiple paths
- **WHEN** a user retakes the survey for a new profession
- **THEN** a new LearningPath with status ACTIVE is created and the old path is set to ARCHIVED

#### Scenario: Cascade delete situations
- **WHEN** a LearningPath is deleted
- **THEN** all associated Situations are also deleted

---

### Requirement: Situation entity
The system SHALL provide a `Situation` entity with fields:
- `title` (String, not null — e.g., "Explaining a delayed task")
- `description` (String, nullable)
- `orderIndex` (Integer, not null — display order within the path)
- `learningPath` (ManyToOne relationship to LearningPath)
- `chunks` (OneToMany relationship to Chunk, cascade ALL)

#### Scenario: Situations ordered within a path
- **WHEN** situations are queried for a learning path
- **THEN** they are retrievable ordered by `orderIndex`

---

### Requirement: Chunk entity
The system SHALL provide a `Chunk` entity with fields:
- `phrase` (String, not null — the phrase/sentence to practice, e.g., "What is the main blocker now?")
- `translation` (String, nullable — translation in user's native language)
- `orderIndex` (Integer, not null — display order within the situation)
- `situation` (ManyToOne relationship to Situation)

#### Scenario: Chunks ordered within a situation
- **WHEN** chunks are queried for a situation
- **THEN** they are retrievable ordered by `orderIndex`

---

### Requirement: PracticeLog entity
The system SHALL provide a `PracticeLog` entity with fields:
- `user` (ManyToOne relationship to User)
- `chunk` (ManyToOne relationship to Chunk)
- `pronunciationScore` (Double, nullable — 0.0 to 100.0)
- `userAudioUrl` (String, nullable — URL/path to recorded audio)
- `practicedAt` (LocalDateTime, not null)

#### Scenario: Practice attempt recorded
- **WHEN** a user completes a pronunciation attempt for a chunk
- **THEN** a PracticeLog is created with the score and timestamp

#### Scenario: Progress tracking
- **WHEN** practice logs are queried for a user and a specific situation
- **THEN** the count of completed (scored) chunks vs total chunks can be determined

---

### Requirement: Level and PathStatus enums
The system SHALL provide:
- `Level` enum with values: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`
- `PathStatus` enum with values: `ACTIVE`, `ARCHIVED`

Both enums SHALL be stored as strings in the database via `@Enumerated(EnumType.STRING)`.

#### Scenario: Enum values persisted as strings
- **WHEN** an entity with a Level or PathStatus field is saved
- **THEN** the database column stores the enum name as a string (e.g., "BEGINNER", "ACTIVE")
