# learning-path-dashboard (Delta Spec)

## MODIFIED

- **REQ-DASHBOARD-SITUATION-CARD-LAYOUT**: Situation cards on `/dashboard` SHALL use a two-column layout on desktop-sized screens, with a left image thumbnail and a right content area organized into a compact product-card flow.
- **REQ-DASHBOARD-SITUATION-CARD-HIERARCHY**: Situation cards SHALL establish a clear reading order of `meta row`, `title`, `short description`, and `bottom action row`, with the CTA as the strongest actionable element.
- **REQ-DASHBOARD-SITUATION-CARD-METADATA**: Situation card metadata such as level and phrase count SHALL be consolidated into a single compact row rather than separated into detached widget blocks.
- **REQ-DASHBOARD-SITUATION-CARD-ACTION-ROW**: Situation cards SHALL include a lighter support/info panel and a visually stronger primary CTA panel, with the CTA clearly receiving more emphasis.
- **REQ-DASHBOARD-SITUATION-CARD-VISUAL-WEIGHT**: Situation cards SHALL reduce unnecessary nested surface treatments so the card feels lighter, cleaner, and more product-oriented.
- **REQ-DASHBOARD-SITUATION-CARD-THEME-READABILITY**: Situation cards SHALL remain easy to read and visually balanced in both light mode and dark mode.
- **REQ-DASHBOARD-SITUATION-CARD-RESPONSIVE**: Situation cards SHALL preserve hierarchy and usability across desktop, tablet, and mobile layouts.

### Scenario: View situation card on desktop
- **WHEN** an authenticated user views the dashboard on a desktop-sized layout
- **THEN** each situation card displays its image in a left media panel
- **AND** the right content panel presents a compact meta row, a prominent title, a short description, and a bottom action row
- **AND** the image occupies a supportive rather than dominant share of the card width

### Scenario: Scan situation card quickly
- **WHEN** an authenticated user glances at a situation card for 2 to 3 seconds
- **THEN** they can identify the level, title, brief purpose, and primary next action without parsing multiple detached blocks
- **AND** the CTA is the clearest interactive target

### Scenario: Consolidated metadata presentation
- **WHEN** an authenticated user views a situation card
- **THEN** level and phrase count appear together in a single compact metadata row
- **AND** phrase count does not appear as a separate standalone widget competing with the card body

### Scenario: Preserve image subject better than banner crop
- **WHEN** a situation card uses a people-centered or portrait-oriented image
- **THEN** the layout preserves more of the important subject framing than the previous top-banner presentation
- **AND** the image supports the card visually without overpowering the title and CTA

### Scenario: CTA outweighs support panel
- **WHEN** an authenticated user views the bottom action row of a situation card
- **THEN** the support/info panel appears visually secondary
- **AND** the primary CTA panel appears clearly more actionable and prominent
- **AND** the CTA affordance is obvious for both mouse and touch interaction

### Scenario: Read card in light and dark mode
- **WHEN** an authenticated user views the dashboard in light mode or dark mode
- **THEN** the situation card preserves strong contrast, clear hierarchy, and readable metadata, title, description, and CTA

### Scenario: View situation card on smaller screens
- **WHEN** an authenticated user views the dashboard on a narrower screen
- **THEN** the situation card adapts responsively without breaking readability
- **AND** the image remains visually meaningful instead of collapsing into a shallow banner
- **AND** the CTA remains clearly associated with the card content
- **AND** the hierarchy remains clear even when the layout stacks vertically
