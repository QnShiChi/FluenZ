## Context

The user wants the product’s primary identity to match the official FluenZ logo rather than the current orange accent direction. The repo already contains `fluenz-logo.png` at the project root, and the user wants it used in the header branding area and favicon.

## Goals / Non-Goals

**Goals:**
- Shift the primary branded accent system from orange to the official logo palette
- Preserve centralized semantic theming so future brand adjustments remain manageable
- Use `fluenz-logo.png` as the official visible brand mark in the app header
- Update favicon branding to match the official logo asset

**Non-Goals:**
- Rebuilding every illustration or decorative asset to match the new palette
- Introducing a brand management admin UI in this change
- Redesigning content structure beyond visual branding changes

## Decisions

### 1. Brand palette follows the official logo gradient
**Choice**: Use the following brand family as the primary source for accent tokens:
- turquoise `#0CCFC3`
- cyan-blue `#0AA4D0`
- indigo `#6050E0`
- violet `#8020F0`

These values should drive primary accents, gradients where needed, focus states, and prominent branded UI moments.

**Why**: This aligns the product with the official logo rather than an arbitrary orange theme.

### 2. Brand tokens remain semantic and centralized
**Choice**: Continue using semantic theme tokens, but map them to the new logo-aligned palette for both light and dark mode.

**Why**: The implementation should stay maintainable and future-friendly.

### 3. Official PNG logo becomes the primary brand mark
**Choice**: Use `fluenz-logo.png` in the main app header branding area instead of the current abstract/icon-only placeholder treatment.

**Why**: The user explicitly wants the official logo asset to represent the brand in the application.

### 4. Favicon uses the official logo asset
**Choice**: Replace the current favicon reference with `fluenz-logo.png` or a favicon asset generated from it while keeping browser compatibility in mind.

**Why**: Browser/tab branding should match the official product mark.

## Files Expected

| File | Change |
|---|---|
| `frontend/src/index.css` | Update semantic brand tokens to logo-aligned palette |
| `frontend/src/components/layout/LearningShell.tsx` | Replace placeholder brand mark in header/shell with official logo image |
| `frontend/index.html` | Update favicon reference and page title branding |
| `frontend/public/...` | Add/copy favicon-compatible logo asset if needed |

## Resolved Inputs

- **Official logo asset**: `fluenz-logo.png` already exists at repo root
- **Branding placement**: apply to header branding and favicon
