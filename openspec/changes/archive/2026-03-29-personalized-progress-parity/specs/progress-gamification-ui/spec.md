## MODIFIED Requirements

### Requirement: Real-time Chunk Completion Feedback
The system MUST provide immediate visual feedback to the user upon successfully finishing a learning chunk, clearly indicating how their daily progress is affected.

#### Scenario: Completing a personalized chunk triggering progress feedback
- **WHEN** the user completes a chunk in `PERSONALIZED` mode
- **THEN** the system MUST display the same style of progress feedback used for `DEFAULT` mode
- **AND** the feedback MUST summarize the realtime time gained for that completion
- **AND** the feedback MUST indicate whether the daily goal or streak state changed
