## Why

The current AI roleplay phase still feels like a guided exercise block instead of a natural, premium tutoring conversation. Messages are rendered as static state changes, the interface exposes too much "phase" behavior, and the overall visual tone does not yet match a high-trust language coach experience. The completion screen also lacks a strong emotional payoff after the learner finishes the AI roleplay phase.

We need to redesign these two moments so the learner feels like they are speaking with a real coach in a live conversation, then receives a brief but memorable sense of achievement when the practice session ends.

## What Changes

- Redesign the AI roleplay phase into a premium coach chat interface that feels closer to a live chatbot conversation than a multi-step exercise panel.
- Keep the experience voice-first, but add an explicit switch that reveals text input only when the learner intentionally changes from speaking mode to typing mode.
- Render AI messages with a frontend-only streaming effect after the full backend response is received, while user messages continue to appear instantly.
- Preserve automatic TTS for AI replies so the learner can continue practicing listening and speaking without extra taps.
- Remove emoji from the roleplay and completion experience to keep the tone clean, premium, and academically polished.
- Add a one-time fireworks celebration that launches upward for roughly 2-3 seconds when the learner enters the session-complete screen after finishing roleplay.
- Refine the completion screen layout so the celebration feels intentional and premium rather than decorative noise.

## Capabilities

### Modified Capabilities
- `practice-ui`: premium chatbot-style AI roleplay surface, voice-first mode switch, streaming AI text reveal, and refined completion experience
- `frontend-scaffolding`: cleaner premium tutoring visual language for high-focus conversation and victory states
- `practice-session`: enhanced completion presentation after the AI roleplay phase finishes

## Impact

- **Frontend**: redesign of the roleplay UI in `RoleplayChatUI.tsx`, completion UX in `PracticePage.tsx`, and likely extraction of smaller conversation/celebration components
- **Interaction design**: voice remains primary, typing becomes an intentional secondary mode, and AI responses feel more live through progressive rendering
- **Motion design**: adds a short upward fireworks celebration on session completion without changing backend completion semantics
- **Backend**: no streaming backend protocol required for this phase; the streaming effect is simulated in the frontend after the response arrives
