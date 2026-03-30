## ADDED Requirements

### Requirement: Multi-step onboarding flow
The system SHALL provide a 7-step progressive onboarding flow for Personalized Learning. Steps are: (1) Job Role, (2) Industry, (3) Seniority or Goal Focus (conditional), (4) Communication targets + channels, (5) Pain Points, (6) Goals, (7) Review + AI Persona Preview.

#### Scenario: User completes professional role onboarding
- **WHEN** user selects a professional job role (e.g., "Software Engineer") on step 1
- **THEN** system proceeds to Industry step, then shows Seniority step on step 3

#### Scenario: User completes student/career-change onboarding
- **WHEN** user selects a student or career-changer role on step 1
- **THEN** system proceeds to Industry step, then shows a goal-focused question on step 3 instead of seniority

#### Scenario: User uses custom input
- **WHEN** user's role or industry is not in the preset list
- **THEN** user can type a custom value using a free-text input field

#### Scenario: User selects multiple items
- **WHEN** user is on Communication, Pain Points, or Goals steps
- **THEN** user can select multiple preset chips and add custom entries

### Requirement: Centralized onboarding config
The system SHALL store all preset choice data (roles, industries, channels, pain points, goals) in a centralized TypeScript config file, not hardcoded in UI components.

#### Scenario: Adding a new industry option
- **WHEN** a developer needs to add a new industry
- **THEN** they edit only the `onboardingConfig.ts` file, no UI component changes needed

### Requirement: Level selection transmitted in payload
The system SHALL include the user-selected English level (`beginner`, `intermediate`, `advanced`) as a required field in the onboarding API request payload. The backend SHALL validate and use this level directly in the LLM prompt.

#### Scenario: User selects Intermediate level
- **WHEN** user selects "Intermediate" in the onboarding flow
- **THEN** the API payload contains `level: "INTERMEDIATE"`, the backend uses "INTERMEDIATE" in the LLM prompt, and generated content difficulty matches intermediate level

#### Scenario: Missing level in payload
- **WHEN** the API request does not include a level field
- **THEN** the backend returns a 400 validation error

### Requirement: AI persona preview on review step
The system SHALL generate a short AI persona summary (3-4 sentences) on the review step using a lightweight LLM call. If the LLM call fails or takes >5 seconds, the system SHALL fall back to a rule-based summary built from the payload fields.

#### Scenario: Successful persona preview
- **WHEN** user reaches the review step and all fields are filled
- **THEN** system calls the persona preview API and displays a generated summary within 5 seconds

#### Scenario: Persona preview timeout/failure
- **WHEN** the LLM persona preview call fails or times out
- **THEN** system displays a rule-based fallback summary (e.g., "You're a mid-level software engineer in fintech who needs to improve client call communication...")

### Requirement: Live summary panel
The system SHALL display a live summary panel showing all selected choices that updates in real-time as the user progresses through onboarding steps.

#### Scenario: User progresses through steps
- **WHEN** user selects role "Software Engineer" on step 1 and industry "Fintech" on step 2
- **THEN** the summary panel shows both selections visible throughout subsequent steps
