## ADDED Requirements

### Requirement: Usable text path SLA
The system SHALL publish the first usable personalized text path within 2 to 3 minutes after the user clicks `Generate My Path`.

#### Scenario: User starts path generation
- **WHEN** the user submits the onboarding review step with `Generate My Path`
- **THEN** the system MUST make a usable personalized text path available within 2 to 3 minutes
- **AND** the UI MUST NOT wait for all thumbnails to finish hydrating before the text path becomes available

### Requirement: Core content prioritized over asset enrichment
The generation pipeline SHALL prioritize usable learning content over thumbnail completion.

#### Scenario: Thumbnail fetching is slower than content generation
- **WHEN** text content is ready before thumbnail hydration completes
- **THEN** the system MUST publish the text path first
- **AND** continue thumbnail hydration asynchronously afterward

### Requirement: Bottleneck-aware orchestration
The generation pipeline SHALL minimize unnecessary sequential waiting and wasteful retries.

#### Scenario: Independent work can run in parallel
- **WHEN** generation includes multiple independent content batches or thumbnail fetch tasks
- **THEN** the system SHOULD run those tasks in parallel where safe
- **AND** retries SHOULD be scoped to the failing batch/item rather than restarting the entire pipeline

### Requirement: Final completion after async hydration
The system SHALL still reach a fully enriched final state after asynchronous processing completes.

#### Scenario: Async hydration completes after text publish
- **WHEN** thumbnail hydration continues after the usable text path has already been published
- **THEN** the final personalized path MUST still converge to 100% thumbnail coverage
- **AND** the final path MUST avoid obvious thumbnail duplication within that user path
