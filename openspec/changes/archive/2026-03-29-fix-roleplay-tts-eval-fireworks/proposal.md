## Why

The newly redesigned roleplay and completion experience still has three critical gaps:

1. The roleplay flow lost important spoken guidance. The opening instruction and the transition cue that hands the conversation back to the learner are no longer played through English TTS.
2. The final roleplay turn is behaving incorrectly. Instead of answering the learner's last question and then evaluating, the AI is jumping straight into evaluation.
3. The evaluation content still contains emoji because the backend roleplay prompt explicitly requests them, which breaks the intended premium academic tone.
4. The completion celebration is not visually delivering as expected. The current custom fireworks effect is not producing the intended result, so the completion screen should switch to a proven library-based implementation.

These issues make the roleplay feel less trustworthy and less polished than intended. We need to correct the conversation contract, remove emoji from evaluation output at the source, and replace the completion fireworks implementation with `@fireworks-js/react`.

## What Changes

- Restore English TTS playback for the roleplay opening guidance and the transition cue that tells the learner it is their turn to ask.
- Correct the final roleplay turn so the AI first answers the learner's last question in English, then appends the Vietnamese evaluation block.
- Remove emoji from the roleplay evaluation prompt, examples, and frontend presentation so the tone remains clean and professional.
- Replace the custom completion fireworks implementation with `@fireworks-js/react`, configured for a short one-time launch from the lower viewport area.
- Keep the rest of the premium chat redesign intact, including voice-first behavior, optional type mode, and frontend-only AI streaming text reveal.

## Capabilities

### Modified Capabilities
- `practice-ui`: restore roleplay guidance TTS, preserve correct conversational turn-taking, and keep evaluation presentation emoji-free
- `practice-session`: replace custom celebration animation with a one-shot `@fireworks-js/react` completion effect

## Impact

- **Backend**: updates `RoleplayService` prompt instructions and examples so final-turn behavior and evaluation tone are correct at the LLM prompt level
- **Frontend**: updates `RoleplayChatUI.tsx` sequencing for instruction TTS and turn handoff, and replaces the completion fireworks component with `@fireworks-js/react`
- **Dependencies**: adds `@fireworks-js/react` for the completion celebration implementation
