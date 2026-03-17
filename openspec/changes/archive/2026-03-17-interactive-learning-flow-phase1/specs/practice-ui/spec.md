## ADDED Requirements

### Requirement: Practice state machine
The frontend SHALL implement a Zustand store (`usePracticeStore`) managing a sequential array of practice steps. Steps MUST execute in order: Phase 1 (4× Quiz + Speak pairs for Root + 3 Chunks) → Phase 2 (3× Full Sentence + Blank pairs for each Chunk).

#### Scenario: Phase 1 flow
- **WHEN** user starts practice
- **THEN** system shows Quiz (State A) for Root phrase, then Speak (State B), then repeats for each Chunk

#### Scenario: Phase 2 flow
- **WHEN** Phase 1 completes
- **THEN** system shows Full Sentence Speaking (State C) then Fill-in-the-blank (State D) for each Chunk

#### Scenario: Step progression
- **WHEN** user completes a step successfully
- **THEN** store advances to next step in the array

### Requirement: Multiple choice quiz component
The frontend SHALL display a MultipleChoiceCard showing Vietnamese translation and 3 English options (1 correct + 2 distractors). A success sound SHALL play on correct selection.

#### Scenario: Correct answer
- **WHEN** user selects the correct English phrase
- **THEN** play "ding" sound and advance to State B (Speak)

#### Scenario: Wrong answer
- **WHEN** user selects a wrong option
- **THEN** highlight the wrong option in red, show the correct answer, allow retry

### Requirement: Speech recognition hook
The frontend SHALL provide a `useSpeechRecognition` hook using `window.SpeechRecognition` (or `webkitSpeechRecognition`) with states: `isListening`, `transcript`, `error`. Language SHALL be set to `en-US`.

#### Scenario: Successful recognition
- **WHEN** user speaks into microphone
- **THEN** hook captures and returns transcript text

#### Scenario: Unsupported browser
- **WHEN** browser does not support Web Speech API
- **THEN** hook returns error state with helpful message

### Requirement: Score display component
The frontend SHALL display evaluation results as a circular ScoreRing (0-100) with word-level feedback. Words SHALL be color-coded: green (CORRECT), red (WRONG/MISSING).

#### Scenario: Score visualization
- **WHEN** backend returns evaluation result
- **THEN** ScoreRing animates to the score value and word list shows color-coded feedback

### Requirement: Practice page route
The frontend SHALL add route `/practice/:situationId` that loads the practice flow for the given situation.

#### Scenario: Navigation from situation detail
- **WHEN** user clicks "Bắt đầu học" on SituationDetailPage
- **THEN** browser navigates to `/practice/{situationId}` and practice begins
