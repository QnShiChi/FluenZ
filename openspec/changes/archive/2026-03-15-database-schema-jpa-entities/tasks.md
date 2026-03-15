## 1. Dependencies & Base Setup

- [x] 1.1 Add Lombok and Jakarta Validation dependencies to `build.gradle.kts`
- [x] 1.2 Create `BaseEntity` abstract class with `id` (UUID), `createdAt`, `updatedAt` fields and JPA lifecycle callbacks

## 2. Enums

- [x] 2.1 Create `Level` enum (BEGINNER, INTERMEDIATE, ADVANCED)
- [x] 2.2 Create `PathStatus` enum (ACTIVE, ARCHIVED)

## 3. JPA Entities

- [x] 3.1 Create `User` entity (email, username, passwordHash, currentLevel, goals) with unique constraints
- [x] 3.2 Create `Profession` entity (name, description) with unique constraint on name
- [x] 3.3 Create `LearningPath` entity (title, status, ManyToOne User, ManyToOne Profession, OneToMany Situations)
- [x] 3.4 Create `Situation` entity (title, description, orderIndex, ManyToOne LearningPath, OneToMany Chunks)
- [x] 3.5 Create `Chunk` entity (phrase, translation, orderIndex, ManyToOne Situation)
- [x] 3.6 Create `PracticeLog` entity (ManyToOne User, ManyToOne Chunk, pronunciationScore, userAudioUrl, practicedAt)

## 4. Repository Interfaces

- [x] 4.1 Create `UserRepository` with `findByEmail` and `findByUsername` methods
- [x] 4.2 Create `ProfessionRepository`
- [x] 4.3 Create `LearningPathRepository` with `findByUserAndStatus` and `findByUserId` methods
- [x] 4.4 Create `SituationRepository` with `findByLearningPathIdOrderByOrderIndex`
- [x] 4.5 Create `ChunkRepository` with `findBySituationIdOrderByOrderIndex`
- [x] 4.6 Create `PracticeLogRepository` with `findByUserIdAndChunkId` and `findByUserIdAndChunkSituationId`

## 5. DTOs

- [x] 5.1 Create Response DTOs: `UserResponse`, `ProfessionResponse`, `LearningPathResponse`, `SituationResponse`, `ChunkResponse`, `PracticeLogResponse`
- [x] 5.2 Create Request DTOs: `CreateUserRequest`, `CreateLearningPathRequest`, `CreateSituationRequest`, `CreateChunkRequest`, `CreatePracticeLogRequest` with validation annotations

## 6. Verification

- [x] 6.1 Rebuild Docker backend image and start services (`make build && make up`)
- [x] 6.2 Verify all tables are auto-created in PostgreSQL via DBeaver or psql
- [x] 6.3 Verify backend health endpoint still returns `{"status": "UP"}`
