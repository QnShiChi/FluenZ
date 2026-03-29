## Why

The current roleplay and completion experience still falls short in three important ways:

1. The roleplay flow still does not reliably speak the opening English guidance cue when the learner enters the AI conversation.
2. The final AI response is still visually combining the English answer and the Vietnamese evaluation into a single message block, which makes the conversation harder to scan and weakens the tutoring feel.
3. The completion fireworks stop automatically after a short time, but the desired experience is for the celebration to continue until the learner actively finishes or leaves the completion screen.

These issues make the roleplay feel less guided at the start, less readable at the end, and less emotionally rewarding on completion. We need to make the handoff clearer, separate the instructional and evaluative content visually, and keep the celebration alive until the learner exits.

## What Changes

- Ensure the roleplay screen always speaks the opening English guidance cue when the learner first enters the AI roleplay step.
- Split the final AI turn into two rendered message blocks:
  - one English answer message
  - one Vietnamese evaluation message
- Allow the Vietnamese evaluation block to render without TTS if it does not sound natural when spoken.
- Keep the fireworks celebration running on the completion screen until the learner presses the completion action or navigates back to the dashboard.
- Preserve the rest of the premium roleplay chat layout and `@fireworks-js/react` fireworks implementation.

## Capabilities

### Modified Capabilities
- `practice-ui`: restore reliable opening guidance TTS and split final AI answer/evaluation into separate chat messages
- `practice-session`: keep completion fireworks running until the learner exits the completion screen

## Impact

- **Backend**: may need stricter response formatting so the frontend can reliably parse answer and evaluation as separate sections
- **Frontend**: updates `RoleplayChatUI.tsx` to speak the opening cue consistently and render the final turn as two blocks; updates completion celebration lifecycle so fireworks persist until exit
- **UX**: clearer roleplay pacing, better transcript readability, stronger persistent sense of achievement on completion
