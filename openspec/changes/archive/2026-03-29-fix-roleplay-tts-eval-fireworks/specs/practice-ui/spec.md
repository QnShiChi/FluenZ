# practice-ui (Delta Spec)

## MODIFIED

- **REQ-ROLEPLAY-GUIDANCE-TTS**: The `ROLEPLAY_CHAT` experience SHALL speak the opening English guidance cue before the first AI question and SHALL speak the English handoff cue before the learner's final-question turn.
- **REQ-ROLEPLAY-FINAL-TURN-ORDER**: The final AI roleplay turn SHALL present an English answer to the learner's question before showing the Vietnamese evaluation block.
- **REQ-ROLEPLAY-EVALUATION-NO-EMOJI**: Roleplay evaluation content shown to the learner SHALL not include emoji.

### Scenario: Opening cue is spoken before first question
- **WHEN** the learner enters the roleplay step
- **THEN** the system speaks an opening English guidance cue
- **AND** the learner then hears the first AI question

### Scenario: Handoff cue is spoken before learner asks
- **WHEN** the AI finishes its first acknowledgment turn
- **THEN** the system speaks an English handoff cue that indicates it is now the learner's turn to ask
- **AND** the learner is then allowed to provide their question

### Scenario: Final turn answers first and evaluates second
- **WHEN** the learner submits their final question in roleplay
- **THEN** the AI response begins with a natural English answer
- **AND** the response only after that includes `---`
- **AND** the Vietnamese evaluation appears after the separator

### Scenario: Evaluation remains emoji-free
- **WHEN** the final roleplay evaluation is displayed
- **THEN** the evaluation text contains no emoji
- **AND** the tone remains clean and professional
