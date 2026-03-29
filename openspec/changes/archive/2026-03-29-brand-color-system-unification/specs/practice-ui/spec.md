# practice-ui (Delta Spec)

## MODIFIED

- **REQ-PRACTICE-BRAND-CONSISTENCY**: Practice cards, supporting chips, score-adjacent surfaces, action states, and selected/focus states SHALL use the unified FluenZ brand token system instead of off-brand orange/beige accents.
- **REQ-PRACTICE-SEMANTIC-STATES**: Practice success, warning, and danger states SHALL remain semantically distinct, but only warning/alert semantics may use orange.

### Scenario: Practice UI aligns with brand
- **WHEN** a user enters the practice flow
- **THEN** the page uses the same teal-cyan-blue-violet visual language as the rest of the product
- **AND** semantic warning/danger feedback remains readable without becoming the default accent system
