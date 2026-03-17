## ADDED Requirements

### Requirement: Practice start payload
The system SHALL provide `GET /api/practice/{situationId}/start` that returns a complete payload containing: rootSentence, rootTranslation, rootIpa, contextQuestion, contextTranslation, and for each variableChunk: text, ipa, distractors[].

#### Scenario: Payload structure
- **WHEN** authenticated user requests practice start for a valid situationId
- **THEN** response contains all chunks with their contextQuestion, rootSentence (with IPA), and variableChunks (with IPA and 2 distractors each)

#### Scenario: Invalid situation
- **WHEN** situationId does not exist
- **THEN** system returns 404 with error message

### Requirement: Practice session completion
The system SHALL provide `POST /api/practice/complete` that saves session metrics including userId, situationId, totalTimeSeconds, overallScore, and failedWords.

#### Scenario: Save session metrics
- **WHEN** user completes a practice session and frontend sends metrics
- **THEN** system creates a PracticeSession record with all metrics and returns the saved session id

### Requirement: LLM prompt generates IPA and distractors
The LLM prompt SHALL request IPA phonetic transcription for rootSentence and each variableChunk. It SHALL also request 2 distractor phrases for each variableChunk and for the rootSentence.

#### Scenario: LLM output includes IPA
- **WHEN** learning path is generated
- **THEN** each chunk has rootIpa, and each variableChunk has ipa and distractors[] fields

### Requirement: Practice session entity
The system SHALL store PracticeSession with fields: user (FK), situation (FK), totalTimeSeconds (int), overallScore (int), failedWords (JSON text), completedAt (timestamp).

#### Scenario: Session persistence
- **WHEN** practice session is saved
- **THEN** data is persisted in practice_sessions table and retrievable by userId
