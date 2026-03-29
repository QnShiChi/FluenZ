## ADDED Requirements

### Requirement: Admin Catalog Tree View Menu
The system SHALL provide a hierarchical navigation menu on the left side of the Admin screen that allows managing and navigating the Catalog structure (`Version` -> `Topic` -> `Situation`).

#### Scenario: Expanding a Topic
- **WHEN** user clicks on a Topic row in the Tree View
- **THEN** the tree expands downward to list all Situations belonging to that Topic.

#### Scenario: Selecting a Node
- **WHEN** user clicks specifically on a Situation or Topic name in the Tree View
- **THEN** the system highlights the selected node in the UI and signals the Workspace view to render the details for that specific node.

#### Scenario: Quick Actions
- **WHEN** user hovers or interacts with a Topic or Situation in the Tree View
- **THEN** quick action icons (e.g., Edit, Delete, Add Child) SHALL appear or remain accessible.
