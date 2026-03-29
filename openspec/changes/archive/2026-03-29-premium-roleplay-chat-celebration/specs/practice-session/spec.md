# practice-session (Delta Spec)

## MODIFIED

- **REQ-PRACTICE-COMPLETION-CELEBRATION**: When the learner enters the session-complete screen after finishing the practice flow, the frontend SHALL trigger a one-time celebration effect that reinforces completion without changing backend completion persistence behavior.
- **REQ-PRACTICE-COMPLETION-FIREWORKS**: The celebration effect SHALL use upward fireworks-style motion from the lower viewport area for approximately 2-3 seconds.
- **REQ-PRACTICE-COMPLETION-NONBLOCKING-MOTION**: Completion celebration motion SHALL not prevent the learner from reading their score, review words, or return action.

### Scenario: Celebration triggers on completion entry
- **WHEN** the learner transitions into the completed practice screen
- **THEN** a one-time celebration effect is triggered automatically
- **AND** it does not require an extra user action

### Scenario: Celebration does not loop forever
- **WHEN** the completion screen remains visible after the initial celebration
- **THEN** the fireworks effect stops after its short run
- **AND** it does not continue looping in the background

### Scenario: Completion semantics remain unchanged
- **WHEN** the frontend displays the celebration
- **THEN** the existing `POST /api/practice/complete` persistence behavior remains unchanged
- **AND** no backend protocol changes are required for the celebration itself
