## MODIFIED Requirements

### Requirement: Multiple choice quiz component
The frontend SHALL display a MultipleChoiceCard showing Vietnamese translation and 3 English options. One option MUST be the correct English `text` from the current Step's target object, and the other two MUST be randomly selected from the `distractors` string (split by `|`) provided in the same target object. If distractors are missing or fewer than 2, the system SHOULD gracefully fall back or handle the error without breaking the UI. A success sound SHALL play on correct selection.

#### Scenario: Correct answer
- **WHEN** user selects the correct English phrase
- **THEN** play "ding" sound and advance to State B (Speak)

#### Scenario: Wrong answer
- **WHEN** user selects a wrong option
- **THEN** highlight the wrong option in red, show the correct answer, allow retry

#### Scenario: Real distractors used
- **WHEN** MultipleChoiceCard renders
- **THEN** options display real distractor data from the backend instead of hardcoded placeholders

### Requirement: Auto-play TTS on step entry
On entering any pronunciation step (SPEAK, FULL_SENTENCE, FILL_BLANK, ROLEPLAY), the system SHALL automatically play TTS audio of the target phrase. The mic button MUST be disabled until TTS playback completes.
Before sending text to the TTS engine, the system MUST strip or replace `___` (underscores used for blanks) to prevent the engine from literally speaking "underscore". If the step is FULL_SENTENCE or FILL_BLANK, the spoken text SHOULD be the completed sentence with the blank filled by the currently learned chunk/phrase. In ROLEPLAY, system UI messages MUST NOT trigger English TTS audio.

#### Scenario: TTS auto-play
- **WHEN** user transitions to a pronunciation step
- **THEN** TTS plays the target phrase automatically and mic button is disabled until playback ends

#### Scenario: TTS underscore handling
- **WHEN** the target text contains `___`
- **THEN** the TTS engine receives text where `___` is either stripped or replaced with the target word, avoiding literal "underscore" pronunciation

#### Scenario: TTS roleplay system messages
- **WHEN** the chat history contains a system instruction (e.g., "Hãy trả lời câu hỏi...")
- **THEN** the TTS engine DOES NOT attempt to read this message

### Requirement: Fill-blank reveal/hide toggle
In FILL_BLANK and FULL_SENTENCE steps, the system SHALL construct and display the full, complete sentence by injecting the target `subPhrase`/`chunk` text into the root sentence's `___` blanks. The system SHALL reveal the full sentence (replace blanks with actual text) after evaluation. The blanked portion SHALL re-hide when user presses mic to retry.

#### Scenario: Reveal after evaluation
- **WHEN** user completes evaluation in a FILL_BLANK step
- **THEN** full sentence is displayed (blanks replaced with actual text)

#### Scenario: Re-hide on retry
- **WHEN** user presses mic to retry in a FILL_BLANK step
- **THEN** blanked portion is hidden again

#### Scenario: Full sentence visible during speaking
- **WHEN** user enters FULL_SENTENCE step
- **THEN** the root sentence displayed on screen has its `___` replaced with the target phrase so the user knows exactly what to say
