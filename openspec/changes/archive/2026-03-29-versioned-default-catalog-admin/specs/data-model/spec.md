# data-model (Delta Spec)

## MODIFIED

- **REQ-USER-ENTITY**: The `User` entity SHALL include a role field and a preferred learning mode field. The role SHALL support at least `USER` and `ADMIN`. The learning mode SHALL support at least `DEFAULT` and `PERSONALIZED`.
- **REQ-LEARNINGPATH-ENTITY**: The `LearningPath` entity SHALL remain user-owned and represent personalized learning content only.

## ADDED

### Requirement: Versioned default catalog model
The system SHALL provide a dedicated versioned default catalog model that is separate from personalized `LearningPath` entities.

The default catalog model SHALL support:
- one logical shared curriculum
- multiple persisted versions
- draft and published lifecycle states
- hierarchical content: topic -> situation -> chunk -> sub-phrase

#### Scenario: Create draft default catalog version
- **WHEN** an admin creates a new default catalog version
- **THEN** the system stores it as a draft version that can be edited and previewed before publish

#### Scenario: Published version retained for learners
- **WHEN** a learner is assigned to a published default catalog version
- **THEN** that assignment remains stable even if a newer version is later published

---

### Requirement: Default catalog version assignment
The system SHALL persist which default catalog version a learner is assigned to for default-mode learning.

#### Scenario: New learner gets current published default version
- **WHEN** a new learner enters the app in default mode
- **THEN** the system assigns the learner to the currently published default catalog version

#### Scenario: Existing learner keeps older version
- **WHEN** a new default catalog version is published
- **THEN** learners already assigned to an older version continue reading that older version until they finish

---

### Requirement: Separate progress stores by content source
The system SHALL track personalized progress separately from default-catalog progress.

#### Scenario: Default progress isolated from personalized progress
- **WHEN** a learner marks a default-catalog sub-phrase as learned
- **THEN** that progress does not modify any personalized-path progress record

#### Scenario: Personalized progress isolated from default progress
- **WHEN** a learner marks a personalized sub-phrase as learned
- **THEN** that progress does not modify any default-catalog progress record

---

### Requirement: Situation and sub-phrase image fields
The system SHALL support image fields for both situation-level and sub-phrase-level presentation.

#### Scenario: Default catalog situation has thumbnail
- **WHEN** an admin uploads a thumbnail for a default situation
- **THEN** the thumbnail URL/path is stored with that default situation

#### Scenario: Personalized situation gets AI-backed image
- **WHEN** a personalized learning path is generated
- **THEN** each generated situation can include a thumbnail URL in addition to any sub-phrase image URLs
