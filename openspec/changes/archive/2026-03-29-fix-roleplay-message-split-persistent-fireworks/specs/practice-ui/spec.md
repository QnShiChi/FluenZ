# practice-ui (Delta Spec)

## MODIFIED

- **REQ-ROLEPLAY-OPENING-CUE-RELIABLE**: The roleplay step SHALL reliably speak the opening English guidance cue when the learner first enters the AI conversation.
- **REQ-ROLEPLAY-FINAL-MESSAGE-SPLIT**: The final AI turn SHALL render the English answer and the Vietnamese evaluation as two separate AI message blocks when both parts are present.
- **REQ-ROLEPLAY-FINAL-EVALUATION-TTS-OPTIONAL**: The Vietnamese evaluation block in the final roleplay turn SHALL NOT require automatic TTS playback.

### Scenario: Opening cue plays on roleplay entry
- **WHEN** the learner enters the roleplay step
- **THEN** the system speaks the opening English guidance cue
- **AND** the cue happens before the first AI prompt is spoken

### Scenario: Final answer and evaluation render separately
- **WHEN** the final AI response contains both an English answer and a Vietnamese evaluation section
- **THEN** the UI shows the English answer in one AI message block
- **AND** the UI shows the Vietnamese evaluation in a separate AI message block immediately after it

### Scenario: Missing separator does not break the UI
- **WHEN** the final AI response does not contain the expected separator
- **THEN** the frontend still renders the response safely as a single AI message
- **AND** the roleplay flow continues without crashing

### Scenario: Evaluation can remain silent
- **WHEN** the Vietnamese evaluation block is rendered
- **THEN** the roleplay flow does not depend on playing TTS for that block
- **AND** the learner can still proceed normally
