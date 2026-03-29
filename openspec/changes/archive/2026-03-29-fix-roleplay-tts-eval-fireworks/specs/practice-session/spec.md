# practice-session (Delta Spec)

## MODIFIED

- **REQ-PRACTICE-COMPLETION-FIREWORKS-IMPLEMENTATION**: The practice completion celebration SHALL use `@fireworks-js/react` rather than the current custom-built fireworks effect.
- **REQ-PRACTICE-COMPLETION-FIREWORKS-BEHAVIOR**: The completion fireworks SHALL be visible on screen entry, launch upward from the lower area, run briefly, and stop automatically without looping forever.

### Scenario: Completion fireworks come from the React library
- **WHEN** the learner enters the practice completion screen
- **THEN** the fireworks effect is rendered using `@fireworks-js/react`
- **AND** the previous custom effect implementation is no longer responsible for the celebration

### Scenario: Fireworks run once and stop
- **WHEN** the completion screen is shown
- **THEN** the fireworks appear for a short celebratory window
- **AND** they stop automatically after the configured duration

### Scenario: Fireworks do not block the summary
- **WHEN** the completion fireworks are active
- **THEN** the learner can still clearly read score summaries, review words, and the return action
