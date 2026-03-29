## Context

The user wants a more vibrant and polished English-learning interface, with a left sidebar similar to the F8 reference and a reusable layout across dashboard and learning screens. The system also needs to be ready for configurable brand color changes and light/dark themes.

## Goals / Non-Goals

**Goals:**
- Provide one shared learning shell for `/dashboard`, `/situations/:id`, and `/practice/:situationId`
- Add a left floating sidebar that starts compact, icon-first, and is extensible for future navigation items
- Refresh the learning UI with an orange-forward palette that feels bright and modern
- Centralize theme colors as tokens so a future admin setting can swap the primary accent without per-page rewrites
- Support both light and dark modes without breaking readability or hierarchy

**Non-Goals:**
- Building the admin UI for theme editing in this change
- Redesigning login/register unless needed for token consistency later
- Changing backend APIs or learning flow business logic

## Decisions

### 1. Shared learning shell across all authenticated learning screens
**Choice**: Introduce a reusable shell component with:
- left floating sidebar
- top content area for page-specific hero/header
- consistent page paddings, max widths, and section spacing

**Why**: Dashboard and learning pages should feel like one product, not separate screens with unrelated layouts.

### 2. Sidebar follows compact F8-inspired navigation
**Choice**: Use an icon-led sidebar with rounded cards/buttons, hover/active states, and room to add more destinations later. Initial destinations should cover learning navigation such as dashboard, progress/roadmap placeholder, retake survey, and sign out.

**Why**: The requested style prioritizes quick recognition, looks more productized, and leaves space for future growth.

### 3. Theme colors move to semantic tokens, not hardcoded per component
**Choice**: Define semantic tokens such as:
- `--brand-primary`
- `--brand-primary-strong`
- `--brand-primary-soft`
- `--surface-base`
- `--surface-elevated`
- `--border-subtle`
- `--text-primary`
- `--text-muted`

Provide token values for both light and dark mode, with orange as the default accent family.

**Why**: This keeps current implementation simple while making future admin-driven color changes realistic.

### 4. Dashboard becomes more visual and summary-driven
**Choice**: Upgrade the dashboard from a plain header + list into:
- a branded hero/header area
- clearer stat cards
- richer topic section headers
- situation cards with stronger hierarchy, hover affordance, and clearer progress metadata

**Why**: This is the user’s first learning screen and should immediately feel motivating and polished.

### 5. Situation and practice screens inherit the same system
**Choice**: Apply the same shell, spacing rhythm, card treatment, and theme tokens to detail and practice screens instead of creating one-off restyles.

**Why**: Shared visual language reduces design drift and makes future theming easier.

## Files Expected

| File | Change |
|---|---|
| `frontend/src/index.css` | Define semantic theme tokens for light/dark modes |
| `frontend/src/components/...` | Add shared learning shell and sidebar components |
| `frontend/src/pages/DashboardPage.tsx` | Rebuild using shared shell and refreshed dashboard sections |
| `frontend/src/pages/SituationDetailPage.tsx` | Wrap in shared shell and align styling |
| `frontend/src/pages/PracticePage.tsx` | Wrap in shared shell and align styling |

## Open Questions Resolved

- **Scope**: Apply the new layout to dashboard and learning screens, not dashboard only
- **Sidebar style**: Use a compact, icon-first, F8-inspired left sidebar
- **Branding direction**: Default to bright orange, while preparing token-based theming and light/dark support
