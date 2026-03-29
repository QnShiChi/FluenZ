## MODIFIED Requirements

### Requirement: Permanent Chunk Completion
The system MUST persistently record when a user finishes all exercises within a specific chunk.

#### Scenario: Completing a personalized chunk
- **WHEN** a user successfully finishes the final step of a learning chunk in `PERSONALIZED` mode
- **THEN** the system MUST create or update a `UserChunkProgress` record setting `is_completed = true`
- **AND** all associated nested item progress inside that chunk MUST be explicitly marked as learned/completed
- **AND** the personalized chunk MUST appear as completed in progress responses used by the UI

#### Scenario: Replaying a completed personalized chunk
- **WHEN** a user completes a personalized chunk that already has `is_completed = true`
- **THEN** the system MUST leave the chunk in the completed state
- **AND** subsequent replays MUST NOT reset or remove the `completed` status
