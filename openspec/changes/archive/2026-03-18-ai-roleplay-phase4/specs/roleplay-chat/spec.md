# roleplay-chat (New Capability)

## Requirements

### Requirement: Roleplay Chat Endpoint
The backend SHALL provide `POST /api/practice/roleplay/chat` that accepts a situationId, chat history, and turnNumber, then returns an AI response.

#### Scenario: Turn 1 — AI acknowledges user answer
- **GIVEN** turnNumber=1, chatHistory contains AI question + user answer
- **WHEN** endpoint is called
- **THEN** LLM generates a concise acknowledgment (max 2 sentences, A2-B1 vocab, no questions)

#### Scenario: Turn 2 — AI answers user question + evaluates
- **GIVEN** turnNumber=2, chatHistory contains full conversation + user question
- **WHEN** endpoint is called
- **THEN** LLM generates: (1) natural answer in English (1-2 sentences), (2) evaluation block in Vietnamese (target chunk usage, grammar corrections, performance summary)

### Requirement: Dynamic System Prompt
The system prompt SHALL be built dynamically from database context:
- **Core Identity**: AI role (from situation), user role, situation description
- **Target Chunks**: Variable chunk texts the user learned in Phases 1-3
- **Turn Rules**: Different instruction sets for turn 1 vs turn 2

### Requirement: Response Format
The endpoint SHALL return `{ "response": "AI text" }`. No streaming. The frontend handles TTS playback.
