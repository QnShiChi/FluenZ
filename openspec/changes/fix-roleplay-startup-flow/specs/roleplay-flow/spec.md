## ADDED Requirements

### Requirement: AI First Interaction
The system MUST allow the AI to initiate the roleplay conversation immediately upon entering the roleplay screen without requiring the user to speak first. The AI's opening statement MUST be derived directly from the scenario's context question.

#### Scenario: User enters Roleplay screen
- **WHEN** the user navigates into the AI Roleplay step of a Practice Room
- **THEN** the AI automatically speaks the `contextQuestion` (in English) as the first turn
- **AND** the UI unlocks the user's microphone so they can respond

### Requirement: Five-Turn Conversation Flow
The system MUST enforce a 5-turn conversation structure to allow for a more natural back-and-forth roleplay.

#### Scenario: Completing a 5-turn roleplay
- **WHEN** the user interacts with the AI coach
- **THEN** the conversation MUST follow exactly these 5 turns:
  1. AI asks opening question
  2. User responds
  3. AI replies to encourage the user
  4. User continues or asks a question
  5. AI responds to the user's last statement and provides the final evaluation
