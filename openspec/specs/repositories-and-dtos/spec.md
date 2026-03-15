## ADDED Requirements

### Requirement: Repository interfaces for all entities
The system SHALL provide Spring Data JPA Repository interfaces for each entity:
- `UserRepository` extends `JpaRepository<User, UUID>`
- `ProfessionRepository` extends `JpaRepository<Profession, UUID>`
- `LearningPathRepository` extends `JpaRepository<LearningPath, UUID>`
- `SituationRepository` extends `JpaRepository<Situation, UUID>`
- `ChunkRepository` extends `JpaRepository<Chunk, UUID>`
- `PracticeLogRepository` extends `JpaRepository<PracticeLog, UUID>`

#### Scenario: Basic CRUD operations
- **WHEN** a repository is used to save, find, update, or delete an entity
- **THEN** the operation succeeds through Spring Data JPA's built-in methods

---

### Requirement: Custom query methods on repositories
The system SHALL provide the following custom query methods:
- `UserRepository.findByEmail(String email)` → `Optional<User>`
- `UserRepository.findByUsername(String username)` → `Optional<User>`
- `LearningPathRepository.findByUserAndStatus(User user, PathStatus status)` → `List<LearningPath>`
- `LearningPathRepository.findByUserId(UUID userId)` → `List<LearningPath>`
- `SituationRepository.findByLearningPathIdOrderByOrderIndex(UUID pathId)` → `List<Situation>`
- `ChunkRepository.findBySituationIdOrderByOrderIndex(UUID situationId)` → `List<Chunk>`
- `PracticeLogRepository.findByUserIdAndChunkId(UUID userId, UUID chunkId)` → `List<PracticeLog>`
- `PracticeLogRepository.findByUserIdAndChunkSituationId(UUID userId, UUID situationId)` → `List<PracticeLog>`

#### Scenario: Find user by email
- **WHEN** `findByEmail` is called with an existing email
- **THEN** the corresponding User is returned

#### Scenario: Find situations by path ordered
- **WHEN** `findByLearningPathIdOrderByOrderIndex` is called
- **THEN** situations are returned sorted by `orderIndex` ascending

---

### Requirement: Response DTOs for all entities
The system SHALL provide Response DTOs that expose safe, client-facing data. DTOs SHALL NOT include sensitive fields (e.g., `passwordHash`) or internal JPA relationships.

Required Response DTOs:
- `UserResponse` (id, email, username, currentLevel, goals, createdAt)
- `ProfessionResponse` (id, name, description)
- `LearningPathResponse` (id, title, status, professionId, professionName, createdAt)
- `SituationResponse` (id, title, description, orderIndex)
- `ChunkResponse` (id, phrase, translation, orderIndex)
- `PracticeLogResponse` (id, chunkId, pronunciationScore, practicedAt)

#### Scenario: User response excludes password
- **WHEN** a UserResponse DTO is created from a User entity
- **THEN** the DTO contains id, email, username, level, goals but NOT passwordHash

---

### Requirement: Request DTOs for create/update operations
The system SHALL provide Request DTOs for data validation:

Required Request DTOs:
- `CreateUserRequest` (email, username, password, currentLevel, goals)
- `CreateLearningPathRequest` (title, professionId)
- `CreateSituationRequest` (title, description, orderIndex, learningPathId)
- `CreateChunkRequest` (phrase, translation, orderIndex, situationId)
- `CreatePracticeLogRequest` (chunkId, pronunciationScore, userAudioUrl)

#### Scenario: Request DTO used for validation
- **WHEN** a CreateUserRequest is received with missing required fields
- **THEN** validation annotations (@NotBlank, @NotNull, @Email) catch the error before reaching the service layer
