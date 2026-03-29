## ADDED Requirements

### Requirement: Permanent Chunk Completion
The system MUST persistently record when a user finishes all exercises within a specific chunk.

#### Scenario: Completing a chunk for the first time
- **WHEN** a user successfully finishes the final step (e.g. AI roleplay) of a learning chunk
- **THEN** the system MUST create or update a `UserChunkProgress` record setting `is_completed = true`
- **AND** all associated nested item progress (e.g., SubPhrases) inside that chunk MUST ALSO be explicitly marked as learned/completed
- **AND** subsequent replays of the same chunk MUST NOT reset or remove the `completed` status
