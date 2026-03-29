## Why

The current learning UI feels plain and fragmented:
1. `Dashboard`, `Situation Detail`, and `Practice` screens do not share a consistent shell or navigation pattern.
2. The visual language is too neutral for an English-learning product and does not feel energetic or memorable.
3. Color decisions are embedded directly in components, which makes future admin-driven brand customization and light/dark support harder.

## What Changes

- Introduce a shared learning app shell with a left floating sidebar inspired by the F8-style navigation pattern.
- Refresh dashboard and learning screens with a brighter orange-led visual system, stronger hierarchy, and more engaging cards/hero areas.
- Move key colors to reusable theme tokens so the primary accent can be changed centrally later from admin configuration.
- Add first-class light and dark theme support for the learning experience.

## Capabilities

### Modified Capabilities
- `frontend-scaffolding`: add shared theme token foundations for configurable branding
- `learning-path-dashboard`: adopt new app shell and refreshed card-based dashboard
- `situation-detail-ui`: adopt new app shell and aligned visual language
- `practice-ui`: adopt new app shell and aligned learning-screen presentation

## Impact

- **Frontend**: shared layout, sidebar navigation, dashboard styling, situation detail styling, practice page styling, theme token setup
- **UX**: navigation becomes more discoverable and consistent across learning flows
- **Future extensibility**: prepares the UI for admin-controlled accent color and theme mode switching
