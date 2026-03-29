# default-learning-catalog (Delta Spec)

## ADDED

### Requirement: Learner-facing default catalog retrieval
The system SHALL provide learner-facing APIs to retrieve the learner's assigned default catalog version with topics, situations, chunks, and sub-phrases.

#### Scenario: Learner reads assigned default catalog version
- **WHEN** a learner in default mode requests their learning content
- **THEN** the system returns the content from that learner's assigned default catalog version

#### Scenario: Learner does not automatically jump to latest version
- **WHEN** a newer default catalog version exists than the learner's assigned version
- **THEN** the learner still receives the older assigned version until reassigned by product rules

---

### Requirement: Default catalog lifecycle
The system SHALL support draft, preview, and publish lifecycle states for default catalog versions.

#### Scenario: Draft version preview
- **WHEN** an admin previews a draft default catalog version in the admin web app
- **THEN** the system returns that draft version without exposing it to learner-facing APIs

#### Scenario: Publish new default version
- **WHEN** an admin publishes a draft default catalog version
- **THEN** the version becomes the active default version for future learner assignments
- **AND** existing learners stay on their already-assigned versions

---

### Requirement: Default mode as initial learner path
The system SHALL make default mode the initial learner path for newly created learner accounts.

#### Scenario: New user starts in default mode
- **WHEN** a newly registered user first enters the application
- **THEN** their preferred learning mode is `DEFAULT`
- **AND** they are routed into the default catalog experience
