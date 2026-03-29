# admin-content-management (Delta Spec)

## ADDED

### Requirement: Full-tree default catalog CRUD
The system SHALL provide admin-only CRUD operations for the full default catalog tree:
- topics
- situations
- chunks
- sub-phrases

Each entity SHALL be editable inside a draft default catalog version.

#### Scenario: Admin edits nested content
- **WHEN** an admin updates a chunk or sub-phrase in a draft version
- **THEN** the changes are saved to that draft version only

#### Scenario: Admin deletes nested content
- **WHEN** an admin deletes a situation from a draft version
- **THEN** its nested draft-only children are removed from that draft version

---

### Requirement: Ordering management
The system SHALL allow admins to manage display order for topics, situations, chunks, and sub-phrases inside a draft version.

#### Scenario: Reorder situations
- **WHEN** an admin updates the order of situations within a topic
- **THEN** learner-facing and preview responses use the updated ordering
