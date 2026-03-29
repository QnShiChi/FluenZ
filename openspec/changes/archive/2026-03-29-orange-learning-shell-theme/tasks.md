## 1. Theme Foundations

- [x] 1.1 Define semantic color tokens for light mode with orange as default primary accent
- [x] 1.2 Define matching semantic color tokens for dark mode
- [x] 1.3 Replace page-level hardcoded accent colors with semantic theme tokens where learning UI depends on branding

## 2. Shared Learning Shell

- [x] 2.1 Create a reusable authenticated learning shell component
- [x] 2.2 Add a left floating sidebar with icon-first navigation and active state
- [x] 2.3 Ensure the shell works responsively on desktop and mobile/tablet

## 3. Dashboard Refresh

- [x] 3.1 Rework dashboard header into a more visual hero/summary area
- [x] 3.2 Upgrade stats and situation cards to match the new visual system
- [x] 3.3 Preserve existing data and navigation behavior while updating layout

## 4. Learning Screen Alignment

- [x] 4.1 Apply the shared shell to the situation detail page
- [x] 4.2 Apply the shared shell to the practice page
- [x] 4.3 Align cards, headings, spacing, and accent usage across learning screens

## 5. Extensibility

- [x] 5.1 Keep brand accent driven by centralized tokens/config instead of per-component hardcoded values
- [x] 5.2 Leave sidebar structure extensible for future destinations such as progress, roadmap, or settings

## 6. Verification

- [ ] 6.1 Verify `/dashboard`, `/situations/:id`, and `/practice/:situationId` in light mode
- [ ] 6.2 Verify `/dashboard`, `/situations/:id`, and `/practice/:situationId` in dark mode
- [ ] 6.3 Verify sidebar usability on smaller screens
