## ADDED Requirements

### Requirement: Published Topics Must Be Learnable Immediately

When the personalized pipeline marks topics as published, those topics SHALL already be durably stored and available to learn immediately.

#### Scenario: Redirect after partial publish

- **WHEN** onboarding redirects the learner away from the generation screen because a personalized path is partially published
- **THEN** the dashboard shows the already published topics immediately
- **AND** those topics can be opened and studied without waiting for the remaining generation work

### Requirement: Published Topic Visibility Must Survive Refresh

The system SHALL preserve visibility of already published topics across refreshes and navigation changes while background generation continues.

#### Scenario: Learner refreshes after partial publish

- **WHEN** the learner refreshes the dashboard after a personalized path has been partially published
- **THEN** the dashboard still shows all topics that were already published
- **AND** the backend continues generating the remaining topics in the background until completion

### Requirement: Generation Monitoring Route Must Exist

The product SHALL provide a dedicated route for monitoring personalized path generation progress after the learner leaves onboarding.

#### Scenario: Learner prefers monitoring over immediate study

- **WHEN** the learner chooses not to start studying immediately after partial publish
- **THEN** the product provides a dedicated generation-progress route
- **AND** that route shows published topic count, remaining topic count, and current generation phase
- **AND** it stays accurate across refreshes

### Requirement: Generated Count Must Not Be Treated As Published Count

The system SHALL distinguish between content that has been generated internally and content that has been durably published for learner access.

#### Scenario: Internal generation progress exceeds published topics

- **WHEN** the backend has generated more topics than it has already published
- **THEN** learner-facing routes use the published topic count for rendering available content
- **AND** they do not expose unfinished or not-yet-persisted topics as learnable
