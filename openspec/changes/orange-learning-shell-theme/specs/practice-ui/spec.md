# practice-ui (Delta Spec)

## MODIFIED

- **REQ-PRACTICE-SHELL**: The practice page at `/practice/:situationId` SHALL use the shared learning app shell with the same left sidebar navigation pattern as other learning screens.
- **REQ-PRACTICE-THEME-AWARE**: Practice UI panels, score surfaces, and supporting controls SHALL use shared semantic theme tokens and support both light and dark modes.
- **REQ-PRACTICE-VISUAL-CONSISTENCY**: The practice experience SHALL align with dashboard and situation detail spacing, card styling, and accent usage while preserving the existing learning flow behavior.

### Scenario: Enter practice with shared shell
- **WHEN** a user opens `/practice/:situationId`
- **THEN** the page appears inside the shared learning shell
- **AND** learning controls remain functional
- **AND** the visual system matches the rest of the learning product
