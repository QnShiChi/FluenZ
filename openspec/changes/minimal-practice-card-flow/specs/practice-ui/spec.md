# practice-ui (Delta Spec)

## MODIFIED

- **REQ-SPEAKING-PRACTICE-CARD**: All speaking-oriented steps (`SPEAK`, `FULL_SENTENCE`, `FILL_BLANK`, `CONTEXT_SPEAK`) SHALL render inside a unified practice card that keeps prompt/media, mic/actions, and result states within the same visible learning block.
- **REQ-INLINE-EVALUATION-STATE**: During speech evaluation, the loading state SHALL appear inline inside the same practice card instead of shifting the user to a separate lower result section.
- **REQ-INLINE-RESULT-FEEDBACK**: After evaluation completes, score feedback, word feedback, reveal content, and retry guidance SHALL appear inside the same practice card, immediately near the microphone/action area.
- **REQ-LOCAL-RETRY-FLOW**: When the user retries after a failed score, the microphone/action area SHALL remain close to the result state so the user can continue without scroll shuttling or losing context.

### Scenario: Speaking step stays in one viewport block
- **WHEN** a user is on any speaking-oriented step
- **THEN** the prompt/media, mic, and result zone are presented in one stable practice card
- **AND** the user does not need to scroll to move between answering and reading feedback

### Scenario: Result appears inline after scoring
- **WHEN** speech evaluation returns a result
- **THEN** the result is displayed in the same practice card, directly near the speaking controls
- **AND** retry messaging remains in the same visible area

### Scenario: Retry remains in-place
- **WHEN** the user retries after a failed attempt
- **THEN** the practice card keeps the user in the same visual flow
- **AND** the user is not forced to jump between distant UI sections
