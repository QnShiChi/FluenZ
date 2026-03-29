## MODIFIED Requirements

### Requirement: Practice session completion
The system SHALL provide `POST /api/practice/complete` that saves session metrics including userId, situationId, totalTimeSeconds, overallScore, and failedWords.

#### Scenario: Save session metrics
- **WHEN** user completes a practice session and frontend sends metrics
- **THEN** system creates a PracticeSession record with all metrics and returns the saved session id
- **AND** the same `totalTimeSeconds` value MUST be available for the chunk-completion progress flow to calculate realtime learning minutes

### Requirement: Practice session entity
The system SHALL store PracticeSession with fields: user (FK), situation (FK), totalTimeSeconds (int), overallScore (int), failedWords (JSON text), completedAt (timestamp).

#### Scenario: Session persistence
- **WHEN** practice session is saved
- **THEN** data is persisted in practice_sessions table and retrievable by userId
