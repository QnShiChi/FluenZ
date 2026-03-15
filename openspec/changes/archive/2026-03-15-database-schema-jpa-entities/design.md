## Context

The FluenZ backend has a Clean Architecture package structure in place but no data model. We need to define JPA entities that support the three core user flows: AI-generated learning paths (Module A), chunking practice (Module B), and AI roleplay scenarios (Module C). Hibernate `ddl-auto: update` is configured, so tables will be auto-created on startup.

## Goals / Non-Goals

**Goals:**
- Define a complete relational data model covering users, professions, learning paths, situations, chunks, and practice logs
- Enforce entity-DTO separation — entities never leak to the API layer
- Use a shared `BaseEntity` for common audit fields (`id`, `createdAt`, `updatedAt`)
- Add Lombok to reduce boilerplate in entities and DTOs
- Provide repository interfaces with basic query methods

**Non-Goals:**
- Implementing REST API endpoints (next change)
- Authentication/authorization (separate change)
- Database migrations with Flyway/Liquibase (not needed yet with `ddl-auto: update`)
- Seed data or test fixtures

## Decisions

### 1. Entity Relationship Design
**Decision:** Use the following relationship model:
```
User (1) ──→ (N) LearningPath (1) ──→ (N) Situation (1) ──→ (N) Chunk
User (1) ──→ (N) PracticeLog
PracticeLog (N) ──→ (1) Chunk
Profession (1) ──→ (N) LearningPath
```
**Rationale:** A user can have multiple learning paths (one per profession retake). Each path contains situations, each situation contains chunks. Practice logs track per-chunk user attempts, linking back to both the user and the specific chunk.

### 2. Base Entity with UUID
**Decision:** Use `UUID` as the primary key type with `@GeneratedValue(strategy = GenerationType.UUID)` in a `BaseEntity` superclass.
**Rationale:** UUIDs prevent ID enumeration attacks and are safe for distributed systems. All entities inherit `id`, `createdAt`, `updatedAt` from `BaseEntity`.

### 3. Lombok for boilerplate reduction
**Decision:** Add Lombok with `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder` on entities and DTOs.
**Rationale:** Eliminates hundreds of lines of getters/setters/constructors. Gradle's `annotationProcessor` config handles compile-time generation.

### 4. Enum for learning level
**Decision:** Use a `Level` enum (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`) stored as a `String` in the database via `@Enumerated(EnumType.STRING)`.
**Rationale:** String storage is more readable in DBeaver and survives enum reordering, unlike ordinal storage.

### 5. Enum for path status
**Decision:** Use a `PathStatus` enum (`ACTIVE`, `ARCHIVED`) on `LearningPath` to support the "retake survey" flow.
**Rationale:** When a user retakes the survey for a new profession, the old path is archived (not deleted) to preserve history.

## Risks / Trade-offs

- **ddl-auto: update in production** → Mitigation: This is dev-only. Before production, we'll switch to Flyway migrations.
- **UUID performance vs Long** → Mitigation: Acceptable for this scale. Can add indexed lookup columns if needed later.
- **Lombok hiding complexity** → Mitigation: Team is familiar with Lombok. Using `@Builder` for clear object construction.
