## Context

FluenZ should look like one cohesive product. Right now the app still contains remnants of orange/beige emphasis and multiple local color decisions. The user wants a full UI color refactor so the system consistently reflects the FluenZ brand palette and feels modern, premium, soft, and educational.

## Goals / Non-Goals

**Goals:**
- Remove orange as a primary accent from normal UI surfaces and interactions
- Standardize all branded UI color roles around teal, cyan, blue, indigo, and violet
- Introduce explicit semantic theme tokens for brand, surfaces, borders, text, and states
- Refactor major components and layouts to rely on tokens instead of ad-hoc hard-coded colors
- Preserve clear hierarchy, contrast, and readability in both light and dark mode

**Non-Goals:**
- Rebuilding the information architecture or changing product behavior
- Turning every element into a gradient-heavy brand surface
- Removing semantic warning/danger colors where they are legitimately useful

## Decisions

### 1. Establish a strict semantic token system
**Choice**: Introduce or normalize tokens such as:
- `--brand-primary`
- `--brand-secondary`
- `--brand-accent`
- `--brand-primary-soft`
- `--brand-secondary-soft`
- `--brand-accent-soft`
- `--surface-soft`
- `--surface-muted`
- `--border-subtle`
- `--text-primary`
- `--text-secondary`
- `--state-success`
- `--state-warning`
- `--state-danger`

Use them as the single source of truth for branded UI styling.

**Why**: This avoids drift and makes maintenance easier.

### 2. Brand hierarchy follows logo family, not orange
**Choice**:
- Primary emphasis: teal / cyan
- Secondary emphasis: blue / indigo
- Accent emphasis: violet / purple
- Orange reserved for warning/alert semantics only

**Why**: This matches the logo and removes mixed brand signals.

### 3. Surface styling should use subtle tints, not loud accents
**Choice**: For regular cards, badges, chips, and highlighted panels:
- use very light cyan, blue, or violet tints
- prefer neutral or softly tinted surfaces
- avoid beige/orange backgrounds unless semantic

**Why**: The UI should feel premium, soft, and calm rather than loud.

### 4. Component states must be standardized
**Choice**: Selected, hover, active, and focus states should use the same color logic across the app:
- selected/active: teal/cyan or violet depending on hierarchy
- hover: subtle tinted backgrounds or border shifts
- focus: brand-consistent ring
- warning/danger: semantic only

**Why**: Consistent interaction language improves perceived polish and usability.

### 5. Refactor by component families, not isolated pages
**Choice**: Audit and refactor color usage across:
- sidebar
- top actions / buttons
- stat cards
- badges
- chips
- topic cards
- practice cards
- borders
- background layers

**Why**: The problem is systemic, so the fix must be systemic too.

## Suggested Palette

### Core Brand
- brand-teal: `#12C6C2`
- brand-cyan: `#1DA1F2`
- brand-indigo: `#6366F1`
- brand-violet: `#8B5CF6`

### Surface Tints
- cyan-tint: `#F0FDFF`
- blue-tint: `#EEF4FF`
- purple-tint: `#F5F3FF`

### Border Tints
- border-cyan: `#D6F4F3`
- border-blue: `#D9E7FF`
- border-purple: `#E7D9FF`

### Text Accents
- text-teal: `#0F766E`
- text-blue: `#2563EB`
- text-violet: `#6D28D9`

## Files Expected

| File | Change |
|---|---|
| `frontend/src/index.css` | Centralize semantic brand/surface/state tokens |
| `frontend/src/components/layout/LearningShell.tsx` | Normalize shell/sidebar/header colors to token system |
| `frontend/src/pages/DashboardPage.tsx` | Replace remaining off-brand tints, badges, and highlight surfaces |
| `frontend/src/pages/SituationDetailPage.tsx` | Align cards, chips, and CTA states with token system |
| `frontend/src/pages/PracticePage.tsx` | Align practice cards and action/result states with token system |
| `frontend/src/components/...` | Replace reusable hard-coded color decisions where needed |

## Implementation Sequence

1. Audit brand-inconsistent colors in current code
2. Define/normalize shared tokens
3. Replace off-brand hard-coded colors in major component families
4. Fine-tune highlight hierarchy without breaking brand consistency
