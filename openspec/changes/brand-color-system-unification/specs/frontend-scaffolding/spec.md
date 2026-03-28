# frontend-scaffolding (Delta Spec)

## MODIFIED

- **REQ-UNIFIED-BRAND-TOKEN-SYSTEM**: The frontend SHALL expose a unified semantic theme token system for brand, surface, border, text, and semantic state colors instead of relying on scattered hard-coded component colors.
- **REQ-BRAND-HIERARCHY**: Primary branded emphasis SHALL use teal/cyan, secondary branded emphasis SHALL use blue/indigo, and accent emphasis SHALL use violet/purple. Orange SHALL NOT serve as a normal primary accent.
- **REQ-SEMANTIC-ORANGE-ONLY**: Orange SHALL be reserved for warning/alert semantics when needed and SHALL NOT be used for normal badges, stat cards, topic cards, chips, or highlight panels.
- **REQ-LIGHT-DARK-CONSISTENCY**: The unified token system SHALL provide visually consistent and readable mappings for both light mode and dark mode.

### Scenario: Normal UI no longer uses orange as main accent
- **WHEN** a user views standard UI surfaces such as navigation, cards, badges, and callouts
- **THEN** those components use the FluenZ brand family of teal, cyan, blue, indigo, and violet
- **AND** orange appears only for semantic warning/alert states where appropriate
