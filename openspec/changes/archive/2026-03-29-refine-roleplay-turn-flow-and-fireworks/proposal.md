## Why

The audio playback issue is resolved, but the roleplay flow still has two major behavioral problems and one UX refinement gap:

1. The opening English guidance cue for the AI roleplay still does not play consistently at the start of the conversation.
2. The final AI turn still jumps directly into evaluation instead of first answering the learner's question in English.
3. The roleplay currently auto-advances to the completion screen, which cuts off the feeling of conversational closure and removes learner control over the transition.
4. The current fireworks effect is present, but it is still too subtle against the product's dominant cool-toned palette and does not feel sufficiently celebratory.

We need to tighten the roleplay turn contract, restore the missing opening cue, add a deliberate learner-controlled transition into completion, and strengthen the fireworks palette so the celebration is immediately visible.

## What Changes

- Fix the AI roleplay opening so the English cue equivalent to "Please answer the following question." is reliably spoken before the first AI message exchange begins.
- Correct the final roleplay turn so the AI first answers the learner's question in English, then appends the Vietnamese evaluation block.
- Stop auto-skipping from the completed roleplay transcript into the completion screen.
- Add a `Tiếp tục` action after the final AI turn finishes, and only transition to the completion/fireworks screen when the learner presses it.
- Strengthen the completion fireworks effect using a more visible, warm, high-contrast color palette that stands out against the app's blue/teal/purple base styling.

## Capabilities

### Modified Capabilities
- `practice-ui`: corrected roleplay opening cue, correct final-turn sequencing, and manual continue gate after roleplay completion
- `practice-session`: stronger, more visible fireworks treatment with a contrasting celebratory palette

## Impact

- **Backend**: may require prompt tightening in `RoleplayService` so turn 2 reliably answers before evaluating
- **Frontend**: updates `RoleplayChatUI.tsx` roleplay completion behavior and start-of-turn cue handling
- **Completion UX**: adjusts fireworks configuration in the React fireworks integration to use a more visible contrasting palette
