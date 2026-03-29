# practice-ui (Delta Spec)

## MODIFIED

- **REQ-ROLEPLAY-PREMIUM-CHAT-SURFACE**: The `ROLEPLAY_CHAT` step SHALL render as a premium tutoring conversation surface with a transcript area, distinct AI and learner message bubbles, and a bottom composer/action area rather than a visibly phased exercise panel.
- **REQ-ROLEPLAY-VOICE-FIRST-MODE**: The roleplay experience SHALL default to a speaking-first mode. A text input field MUST remain hidden until the learner explicitly switches to typing mode.
- **REQ-ROLEPLAY-TYPE-FALLBACK**: The learner SHALL be able to switch to a typing mode and send text responses through the same roleplay turn flow as spoken responses.
- **REQ-AI-STREAMING-PRESENTATION**: AI replies in the roleplay step SHALL be presented with a frontend-only progressive text reveal after the complete backend response is received. Learner messages SHALL appear instantly.
- **REQ-ROLEPLAY-AUTO-TTS**: AI replies in the roleplay step SHALL continue to auto-play through TTS, even when the displayed text is revealed progressively.
- **REQ-ROLEPLAY-NO-EMOJI-TONE**: Roleplay and completion UI copy within the practice experience SHALL avoid emoji and maintain a clean, premium academic tone.

### Scenario: Roleplay opens as conversation UI
- **WHEN** the learner enters a `ROLEPLAY_CHAT` step
- **THEN** the screen presents a tutor-like chat layout with transcript history and a composer area
- **AND** the UI does not primarily read as a wizard-like phase panel

### Scenario: Speak mode is primary
- **WHEN** the roleplay step first loads
- **THEN** the composer defaults to `Speak` mode
- **AND** the microphone action is the primary visible action
- **AND** the text input is not shown

### Scenario: Type mode reveals input on demand
- **WHEN** the learner switches from `Speak` to `Type`
- **THEN** a text input and send action are shown
- **AND** the learner can submit a typed message without using speech recognition

### Scenario: AI response streams visually after response arrives
- **WHEN** the frontend receives a full AI reply from the backend
- **THEN** the UI renders the reply through a progressive local reveal effect inside the AI bubble
- **AND** the learner does not see the entire message appear at once

### Scenario: Learner message appears instantly
- **WHEN** the learner submits speech or typed input
- **THEN** the learner message is inserted into the transcript immediately without streaming animation

### Scenario: Guidance stays outside main transcript when possible
- **WHEN** the system needs to tell the learner to wait, speak, or type
- **THEN** the guidance is presented as subtle status text or helper content near the composer
- **AND** the main transcript remains focused on AI and learner messages

### Scenario: Roleplay copy remains premium and emoji-free
- **WHEN** the roleplay and completion surfaces render text labels and helper copy
- **THEN** emoji are not used
- **AND** the overall tone remains clean, calm, and academically polished
