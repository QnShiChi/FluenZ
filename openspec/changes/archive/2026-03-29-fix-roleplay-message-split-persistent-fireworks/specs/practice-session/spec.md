# practice-session (Delta Spec)

## MODIFIED

- **REQ-PERSISTENT-COMPLETION-FIREWORKS**: The completion fireworks celebration SHALL remain active while the learner stays on the completion screen.
- **REQ-COMPLETION-FIREWORKS-STOP-ON-EXIT**: The completion fireworks SHALL stop when the learner exits the completion screen through the completion action, dashboard navigation, or screen unmount.

### Scenario: Fireworks stay active on completion screen
- **WHEN** the learner remains on the completion screen
- **THEN** the fireworks continue running instead of auto-stopping after a short timeout

### Scenario: Fireworks stop when learner leaves
- **WHEN** the learner presses the completion action or navigates back to the dashboard
- **THEN** the fireworks stop as part of leaving the completion screen

### Scenario: Fireworks do not depend on a short timer
- **WHEN** the completion screen stays mounted for longer than a few seconds
- **THEN** the celebration remains active until the learner exits
