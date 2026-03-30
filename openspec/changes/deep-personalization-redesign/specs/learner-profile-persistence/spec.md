## ADDED Requirements

### Requirement: LearnerProfile entity with raw and normalized data
The system SHALL create a `LearnerProfile` entity that stores both the raw onboarding payload (as JSON) and normalized fields for analytics and querying.

#### Scenario: Profile created during onboarding
- **WHEN** user completes onboarding and generates a learning path
- **THEN** a `LearnerProfile` record is created with `rawPayload` (full JSON), `jobRole`, `industry`, `seniority`, `level`, and `personaSummary` fields

#### Scenario: Profile linked to learning path
- **WHEN** a learning path is generated
- **THEN** the `LearningPath` entity has a FK reference to the `LearnerProfile` used to generate it

### Requirement: Raw payload enables regeneration
The system SHALL store the complete onboarding request payload as JSON, enabling future path regeneration from the same inputs.

#### Scenario: Regeneration from stored profile
- **WHEN** a developer queries a learning path's associated profile
- **THEN** the `rawPayload` field contains the exact JSON that was submitted during onboarding, deserializable back to the request DTO
