# learning-path-dashboard (Delta Spec)

## MODIFIED

- **REQ-DASHBOARD-SHELL**: The learning path dashboard at `/dashboard` SHALL render inside a shared learning app shell with a left floating sidebar.
- **REQ-DASHBOARD-VISUAL-REFRESH**: The dashboard SHALL use a brighter, more engaging visual treatment with a branded hero area, elevated stat summaries, and richer situation cards.
- **REQ-DASHBOARD-NAVIGATION**: The left sidebar SHALL support icon-first navigation and clearly indicate the active destination.
- **REQ-DASHBOARD-THEME-AWARE**: The dashboard SHALL render correctly in both light and dark modes using shared semantic theme tokens.

### Scenario: View refreshed dashboard
- **WHEN** an authenticated user with an ACTIVE learning path visits `/dashboard`
- **THEN** the page displays inside the shared learning shell
- **AND** the left sidebar is visible
- **AND** dashboard sections use the refreshed branded styling
- **AND** existing topic and situation data remains accessible
