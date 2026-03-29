# frontend-scaffolding (Delta Spec)

## MODIFIED

- **REQ-BRAND-TOKENS-LOGO-ALIGNED**: The frontend SHALL map its centralized brand tokens to the official FluenZ logo palette, using turquoise `#0CCFC3`, cyan-blue `#0AA4D0`, indigo `#6050E0`, and violet `#8020F0` as the core branded accent family.
- **REQ-OFFICIAL-LOGO-ASSET**: The frontend SHALL use `fluenz-logo.png` as the official product logo in the application header branding area.
- **REQ-FAVICON-BRANDING**: The frontend SHALL use the official FluenZ logo asset, or a favicon-compatible derivative of it, for browser tab branding.

### Scenario: Header uses official logo
- **WHEN** a user opens the application
- **THEN** the header branding displays the official `fluenz-logo.png` asset
- **AND** the visible brand styling matches the logo-aligned palette

### Scenario: Browser tab reflects new branding
- **WHEN** the application is loaded in the browser
- **THEN** the favicon reflects the official FluenZ logo branding instead of the previous default asset
