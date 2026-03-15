## Why

The FluenZ platform infrastructure is set up (Docker, Spring Boot, React), but the backend has no data model. All three core modules (Onboarding/Path Generation, Chunking Practice, AI Roleplay) depend on a well-defined database schema. Without JPA entities, repositories, and DTOs, no feature APIs can be built. This is the foundational data layer that everything else builds on.

## What Changes

- Create JPA entities: `User`, `Profession`, `LearningPath`, `Situation`, `Chunk`, `PracticeLog`
- Define entity relationships (User → LearningPath → Situation → Chunk, User → PracticeLog)
- Create Spring Data JPA Repository interfaces for each entity
- Create Request/Response DTOs with strict separation from entities
- Add base entity class with common audit fields (`id`, `createdAt`, `updatedAt`)
- Add Lombok dependency to reduce entity boilerplate

## Capabilities

### New Capabilities
- `data-model`: JPA entities, entity relationships, base entity class, and audit fields covering the full domain model (Users, Professions, LearningPaths, Situations, Chunks, PracticeLogs)
- `repositories-and-dtos`: Spring Data JPA repositories for all entities and Request/Response DTOs for API communication

### Modified Capabilities
_(none)_

## Impact

- **Backend**: New files in `entity/`, `repository/`, `dto/request/`, `dto/response/` packages
- **Database**: Hibernate `ddl-auto: update` will auto-create tables on next startup
- **Dependencies**: Adding Lombok to `build.gradle.kts`
- **No API changes yet** — endpoints will be added in subsequent changes
