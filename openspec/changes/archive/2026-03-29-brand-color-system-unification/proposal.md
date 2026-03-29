## Why

The current UI still mixes multiple visual color languages:
1. Some surfaces and components still carry orange/beige influence from earlier iterations.
2. Brand-aligned colors are not yet consistently applied across badges, cards, selected states, buttons, and shell chrome.
3. Hard-coded accents and ad-hoc tints make the system harder to maintain and visually inconsistent.

This causes the product to feel less polished and less aligned with the official FluenZ identity.

## What Changes

- Audit the existing UI color usage and identify places where orange/beige or non-brand hard-coded accents still act as primary emphasis.
- Introduce a clearer shared theme token system centered on FluenZ brand colors:
  - primary: teal/cyan
  - secondary: blue/indigo
  - accent: violet
- Refactor major UI surfaces and interactive states to consume those tokens instead of mismatched hard-coded colors.
- Restrict orange usage to semantic warning/alert roles only when actually needed.

## Capabilities

### Modified Capabilities
- `frontend-scaffolding`: centralized brand token system and semantic UI color roles
- `learning-path-dashboard`: token-based card/badge/button styling aligned with brand
- `practice-ui`: token-based state surfaces and control emphasis aligned with brand
- `situation-detail-ui`: token-based section/card styling aligned with brand

## Impact

- **Frontend**: `index.css`, shared shell/layout, dashboard, situation detail, practice UI, and common branded surfaces
- **Design system**: improved maintainability, consistency, and brand coherence in both light and dark mode
