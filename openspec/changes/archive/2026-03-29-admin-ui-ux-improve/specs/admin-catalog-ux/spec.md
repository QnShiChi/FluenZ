## ADDED Requirements

### Requirement: Hierarchical Data Accordion
The Admin Catalog page SHALL display Topics, Situations, and Chunks using collapsible Accordion components to prevent excessive vertical scrolling.

#### Scenario: Expanding a Topic
- **WHEN** the user clicks on a Topic header
- **THEN** the Situation list for that Topic expands natively without reloading

### Requirement: Overlay Edit Forms
The Admin Catalog page SHALL utilize a Slide-out Sheet or Modal Dialog for all Create and Update forms (Topic, Situation, Chunk, SubPhrase) rather than pushing content down linearly.

#### Scenario: Editing a Situation
- **WHEN** the user clicks "Sửa situation"
- **THEN** a Sheet overlay slides in from the right edge containing the edit form
- **AND** the main page retains its scroll state and layout
