# practice-ui (Delta Spec)

## MODIFIED

- **REQ-ROLEPLAY-OPENING-CUE-RELIABILITY**: The AI roleplay experience SHALL reliably play the opening English guidance cue before the learner is asked to respond to the first AI question.
- **REQ-ROLEPLAY-FINAL-TURN-ANSWER-FIRST**: The final AI roleplay turn SHALL visibly answer the learner's question in English before presenting the Vietnamese evaluation block.
- **REQ-ROLEPLAY-MANUAL-CONTINUE-GATE**: After the final AI turn has fully rendered and finished playback, the roleplay SHALL not auto-transition away. Instead, it SHALL enter a completed state that requires the learner to click `Tiếp tục`.

### Scenario: Opening cue plays before first response state
- **WHEN** the learner enters the AI roleplay step
- **THEN** the opening English cue is spoken before the learner can respond
- **AND** the first AI question is delivered after that cue sequence

### Scenario: Final roleplay turn answers before evaluating
- **WHEN** the learner submits the final question in roleplay
- **THEN** the AI message begins with an English answer
- **AND** only after that does the message include `---`
- **AND** the Vietnamese evaluation appears after the separator

### Scenario: Conversation waits for learner confirmation before completion
- **WHEN** the final AI turn finishes streaming and TTS playback
- **THEN** the roleplay remains on the transcript screen
- **AND** a `Tiếp tục` action becomes available
- **AND** the learner must press it before entering the completion screen

### Scenario: Inputs are disabled after roleplay is complete
- **WHEN** the roleplay enters its completed-awaiting-continue state
- **THEN** microphone and typed input are no longer available
- **AND** the learner can focus on the final exchange before moving on
