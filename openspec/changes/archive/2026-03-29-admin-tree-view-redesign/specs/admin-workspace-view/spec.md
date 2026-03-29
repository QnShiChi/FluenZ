## ADDED Requirements

### Requirement: Admin Catalog Workspace Hub
The system SHALL dedicate the main (right) area of the Admin interface to a dynamic Workspace that changes format based on the node currently selected from the Tree View.

#### Scenario: Topic Selection
- **WHEN** the user selects a Topic in the Tree View
- **THEN** the Workspace displays the Topic's settings form (Name editing) and aggregated statistics (e.g., number of situations).

#### Scenario: Situation Selection
- **WHEN** the user selects a Situation in the Tree View
- **THEN** the Workspace exclusively displays the Situation's settings form, cover image upload, and the list of its associated Chunks and SubPhrases in a flat, infinitely scrollable container that is NOT constrained by Accordion overflow rules.

#### Scenario: Inline Entity Editing
- **WHEN** the user is viewing a Situation in the Workspace and clicks "Edit Chunk"
- **THEN** the chunk is replaced inline by a flat editing form (without popping out full Sheets or Drawers overlapping the entire screen).
