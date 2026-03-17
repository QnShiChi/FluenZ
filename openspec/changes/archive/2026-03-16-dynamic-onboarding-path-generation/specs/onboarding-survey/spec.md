## ADDED Requirements

### Requirement: Topic entity
The system SHALL provide a `Topic` entity with fields:
- `name` (String, not null — e.g., "Báo Cáo Tiến Độ Với Team", "Trao Đổi Code")
- `orderIndex` (Integer, not null — display order within the path)
- `learningPath` (ManyToOne → LearningPath)
- `situations` (OneToMany → Situation, cascade ALL)

---

### Requirement: SubPhrase entity
The system SHALL provide a `SubPhrase` entity with fields:
- `text` (String, not null — e.g., "due to new requirements")
- `orderIndex` (Integer, not null)
- `chunk` (ManyToOne → Chunk)

---

### Requirement: Situation updated to link to Topic
The `Situation` entity SHALL change its parent relationship from `LearningPath` (ManyToOne) to `Topic` (ManyToOne). A new `level` field (Level enum) SHALL be added to Situation.

---

### Requirement: Survey API endpoint
The system SHALL provide `POST /api/onboarding/generate` (protected) that:
- Accepts: professionId, communicationContexts (list of strings), specificGoals (optional text)
- Uses the authenticated user's level from their profile
- Triggers LLM path generation
- Saves the generated path (LearningPath → Topics → Situations → Chunks → SubPhrases)
- Archives any existing ACTIVE path for the user
- Returns 201 with the generated LearningPathResponse
- Returns loading state while LLM processes (3-10 seconds)

#### Scenario: First-time survey
- **WHEN** a new user completes the survey
- **THEN** a new LearningPath with ACTIVE status is created with AI-generated content

#### Scenario: Retake survey
- **WHEN** a user with an existing ACTIVE path completes the survey again
- **THEN** the old path is ARCHIVED and a new ACTIVE path is created

---

### Requirement: Profession list endpoint
The system SHALL provide `GET /api/professions` (protected) that returns all professions.

---

### Requirement: Profession data seeding
The system SHALL seed ~10 professions on startup if the professions table is empty:
Software Engineer, Marketing, Finance & Accounting, Human Resources, Sales, Customer Service, Healthcare, Education, Design, F&B/Hospitality.

---

### Requirement: Learning path retrieval endpoints
The system SHALL provide:
- `GET /api/learning-paths/active` — returns the user's active learning path with topics
- `GET /api/learning-paths/{id}/topics` — returns all topics for a path
- `GET /api/topics/{id}/situations` — returns situations for a topic (ordered by orderIndex)
- `GET /api/situations/{id}/chunks` — returns chunks for a situation with subPhrases
