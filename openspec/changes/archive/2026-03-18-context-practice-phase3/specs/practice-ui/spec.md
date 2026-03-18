# practice-ui (Delta Spec)

## Changes

### Added Requirements

- **REQ-CONTEXT-SPEAK**: New `CONTEXT_SPEAK` step type. TTS plays the context question. Screen shows only context question text + context image. No answer hints visible. Mic disabled until TTS finishes.
- **REQ-CONTEXT-ANSWER-REVEAL**: After evaluation (any score), always reveal the correct full sentence answer on screen for reinforcement. When user presses mic to retry, hide the answer again.
- **REQ-PHASE3-STEPS**: Phase 3 steps (3× `CONTEXT_SPEAK`) SHALL be appended after Phase 2 in the step generation logic. One step per variable chunk: context question → expected answer is full sentence (root + chunk).
