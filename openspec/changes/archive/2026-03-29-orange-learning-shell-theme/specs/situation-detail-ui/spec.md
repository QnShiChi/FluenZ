# situation-detail-ui (Delta Spec)

## MODIFIED

- **REQ-SITUATION-SHELL**: The situation detail page SHALL use the shared learning app shell with a left floating sidebar.
- **REQ-SITUATION-VISUAL-REFRESH**: The page SHALL adopt the refreshed branded visual system for section headers, cards, and call-to-action areas.
- **REQ-SITUATION-THEME-AWARE**: The page SHALL support both light and dark modes using the same semantic theme tokens as dashboard and practice pages.

### Scenario: View situation detail inside shared shell
- **WHEN** a user opens `/situations/:id`
- **THEN** the page appears inside the shared learning shell
- **AND** existing situation content remains available
- **AND** the page styling matches the refreshed dashboard and practice screens
