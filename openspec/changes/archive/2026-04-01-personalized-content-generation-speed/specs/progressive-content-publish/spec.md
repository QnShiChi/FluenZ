## ADDED Requirements

### Requirement: Core Learning Content Must Be Separated From Enrichment

The personalized generation pipeline SHALL distinguish `core learning content` from `enrichment content`.

#### Scenario: Core content is sufficient to start learning

- **WHEN** a chunk is considered publishable
- **THEN** it contains the core fields needed for the learner to begin
- **AND** optional enrichment fields may still be pending

### Requirement: Core Learning Content Must Be Available Before Enrichment

The system SHALL prioritize generation and publication of core learning content ahead of optional enrichment fields.

#### Scenario: Learner starts before enrichment completes

- **WHEN** a publishable topic window is ready
- **THEN** the learner can access it immediately
- **AND** missing enrichment fields do not block path publication

### Requirement: Publish Window Must Be Durable

The first published personalized content window SHALL be durably persisted and remain accessible while the rest of the pipeline continues.

#### Scenario: Background generation continues after initial publish

- **WHEN** the backend has published the first portion of the path
- **THEN** that portion remains available to the learner
- **AND** background generation state is tracked separately from the already published content

