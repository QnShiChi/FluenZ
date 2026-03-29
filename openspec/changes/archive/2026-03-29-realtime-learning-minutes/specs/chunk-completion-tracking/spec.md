## MODIFIED Requirements

### Requirement: Permanent Chunk Completion
The system MUST persistently record when a user finishes all exercises within a specific chunk.

#### Scenario: Completing a chunk for the first time
- **WHEN** a user successfully finishes the final step of a learning chunk
- **THEN** the system MUST create or update a `UserChunkProgress` record setting `is_completed = true`
- **AND** subsequent replays of the same chunk MUST NOT reset or remove the `completed` status

#### Scenario: Replaying a completed chunk
- **WHEN** a user completes a chunk that already has `is_completed = true`
- **THEN** the system MUST leave the chunk in the completed state
- **AND** the chunk MUST continue to appear as completed in progress responses used by the UI
