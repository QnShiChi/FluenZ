## Why

The current redesign improves image treatment, but the card still feels too close to a visual showcase rather than an efficient product card for everyday dashboard use.

Current issues:

1. The image still attracts too much attention relative to the title and CTA.
2. Metadata such as level and phrase count feels too fragmented.
3. Too many inner blocks compete for emphasis, which weakens hierarchy.
4. The info panel and CTA panel currently have similar visual weight, so the primary action does not stand out enough.
5. The card feels heavier than necessary because multiple nested surfaces create a “card inside card inside widget” effect.

We want a situation card that still feels premium and editorial, but is ultimately more product-focused: easier to scan in 2–3 seconds, clearer in both light and dark mode, and cleaner to scale in a real dashboard.

## What Changes

- Refine dashboard situation cards into a more compact two-column product card with image on the left and a tighter content stack on the right.
- Reduce image dominance so it supports recognition without overpowering the title and CTA.
- Consolidate metadata into a single compact meta row instead of separate widgets.
- Establish a clearer reading flow: meta row, title, short description, bottom action row.
- Make the bottom action row asymmetric so the support/info panel is visually lighter and the CTA is the strongest element in the card.
- Reduce visual noise by removing unnecessary nested card treatments and relying more on spacing, typography, and restrained surfaces.
- Keep the card maintainable and extensible for future status signals such as progress, completion, or in-progress state.

## Capabilities

### Modified Capabilities
- `learning-path-dashboard`

## Impact

- **Frontend**: refactor the dashboard situation card structure, spacing, action row, and responsive behavior
- **UX**: faster scanability, clearer CTA priority, and lower visual fatigue
- **Design system**: pushes the card toward a reusable product-card pattern rather than a one-off showcase layout
