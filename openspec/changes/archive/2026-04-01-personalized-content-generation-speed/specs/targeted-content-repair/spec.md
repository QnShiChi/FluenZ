## ADDED Requirements

### Requirement: Validation Must Operate At Finer Granularity Than Batches

The personalized content pipeline SHALL validate generated content at chunk and situation granularity rather than only at large batch granularity.

#### Scenario: One bad chunk does not invalidate an entire topic batch

- **WHEN** one chunk fails validation
- **THEN** the system isolates that chunk failure
- **AND** the rest of the already valid content is preserved

### Requirement: Retry Scope Must Be Narrow

The system SHALL retry only the smallest failing generation unit that can safely be repaired.

#### Scenario: Chunk-level repair

- **WHEN** a single chunk is invalid but the surrounding situation is otherwise valid
- **THEN** the system retries or repairs only that chunk
- **AND** it does not regenerate unrelated chunks

#### Scenario: Situation-level retry

- **WHEN** a situation fails parse or structural validation
- **THEN** the system retries only that situation
- **AND** it does not regenerate unrelated situations or topics unless repeated failure escalation is required

### Requirement: Batch-Level Retry Must Be A Last Resort

The personalized pipeline SHALL avoid large batch regeneration unless narrower repair strategies have failed.

#### Scenario: Narrow repair is attempted first

- **WHEN** a validation error occurs
- **THEN** chunk-level or situation-level repair is attempted before any larger-scope regeneration
