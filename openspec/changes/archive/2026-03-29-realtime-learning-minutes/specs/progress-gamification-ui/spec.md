## MODIFIED Requirements

### Requirement: Real-time Chunk Completion Feedback
The system MUST provide immediate visual feedback to the user upon successfully finishing a learning chunk, clearly indicating how their daily progress is affected.

#### Scenario: Completing a chunk triggering a Toast
- **WHEN** the user completes the final step of a chunk and returns to the situation dashboard
- **THEN** the system MUST display a floating Toast notification stating the chunk is completed
- **AND** the notification MUST summarize the realtime time gained for that completion after rounding and cap are applied
- **AND** the notification MUST indicate whether the 5-minute daily goal has been hit or the streak has been extended

### Requirement: Completed Chunk Visual Distinction
The system MUST visually differentiate chunks that have been successfully completed from those that are not started or in-progress.

#### Scenario: Viewing a completed chunk
- **WHEN** a user looks at the list of chunks for a situation
- **THEN** any chunk marked as `completed` MUST display a distinct visual treatment (e.g., green border, checkmark icon, or "Đã hoàn thành" badge)
- **AND** the user MUST still be able to click and replay the completed chunk without losing its permanent `completed` status
