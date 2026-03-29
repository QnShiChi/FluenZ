## Overview

This change refines the behavior of the premium AI roleplay flow after the earlier redesign and bug-fix passes. The goal is not to redesign the roleplay experience again, but to correct the remaining sequence issues and improve the emotional handoff into completion.

The refinement has three parts:

1. Make the opening English cue reliably audible at roleplay start.
2. Ensure the final AI turn answers the learner's question before evaluation and gives the learner control over when to leave the conversation.
3. Make the completion fireworks visually stronger through a warmer, higher-contrast palette.

## Goals

- Restore reliable playback of the opening roleplay cue
- Preserve the intended final-turn structure:
  - English answer first
  - separator
  - Vietnamese evaluation second
- Prevent roleplay from auto-jumping into completion
- Add a clear learner-controlled `Tiếp tục` action after the conversation ends
- Make fireworks more visible against the app's cool-toned UI

## Non-Goals

- No broader rewrite of the premium roleplay layout
- No changes to practice scoring or persistence contracts
- No replacement of the existing React fireworks integration

## Current Problems

### 1. Opening Cue Still Not Reliably Audible

Even after the audio fixes, the roleplay opening cue equivalent to `Please answer the following question.` is still not consistently heard at the beginning of the AI roleplay. This weakens onboarding into the conversation and makes the first transition feel abrupt.

### 2. Final Turn Still Feels Wrong

The final AI turn is still behaving as if evaluation dominates the response. The desired structure is:

1. answer the learner's question in English
2. show `---`
3. evaluate in Vietnamese

This must be enforced clearly enough that the learner experiences a real conversational answer before receiving feedback.

### 3. Auto-Advance Removes Conversational Closure

The roleplay transcript currently finishes and then moves on too quickly. That makes the final answer feel disposable and gives the learner no chance to absorb the last exchange before entering the summary screen.

### 4. Fireworks Still Blend Too Much into the Existing Theme

The current celebration effect exists, but its visual palette is still too close to the product's dominant blue/teal/purple tones. The effect should contrast more clearly so the celebration is noticeable on first glance.

## Proposed Design

### 1. Reliable Opening Cue Before Conversation Starts

The opening English cue should be treated as a required pre-conversation event, not a best-effort enhancement.

Desired sequence:

1. play opening cue in English
2. once cue completes, render and speak the first AI question
3. only then unlock the learner response state

The learner should never enter the first response state before the cue and first AI prompt have completed.

### 2. Enforce Final-Turn Answer-Then-Evaluation Contract

The backend roleplay prompt should be tightened again if necessary so the model must:

- directly answer the learner's last question in English first
- avoid starting with evaluation language
- then add `---`
- then provide the Vietnamese evaluation block

Frontend should continue to treat the final response as one AI message, but conceptually it must remain a two-part response.

### 3. Add a Manual Continue Gate After Roleplay Completion

When the final AI turn has fully:

- streamed into the transcript
- finished TTS playback

the roleplay should enter a `completed-awaiting-continue` state.

In this state:

- the transcript remains visible
- learner inputs are disabled
- a clear `Tiếp tục` button appears
- pressing `Tiếp tục` transitions to the completion screen

This preserves learner control and gives the final answer time to land.

### 4. Strengthen Fireworks Palette

Keep `@fireworks-js/react`, but tune its options toward a warmer celebratory palette that contrasts with the base application colors.

Recommended palette direction:

- amber
- orange
- gold
- rose

These tones should remain premium rather than playful, but they need enough contrast to stand out clearly against cool blue surfaces.

## State and Flow Notes

### Roleplay

Introduce or clarify a terminal UI state after final AI playback:

- `completed-awaiting-continue`

Behavior in this state:

- no more mic input
- no more typed input
- no further AI calls
- only transcript review + `Tiếp tục`

### Completion

No persistence or routing contract changes beyond delaying the transition until the learner clicks `Tiếp tục`.

## Risks

- If the final response remains model-inconsistent, the learner may still perceive evaluation as dominant even with a better prompt
- If `Tiếp tục` appears too early, it can interrupt the final TTS
- If fireworks become too saturated, the premium visual tone can slip into a noisy celebration aesthetic

## Resolved Decisions

- Keep the premium roleplay UI
- Keep the React fireworks integration
- Add a manual continue gate after final AI turn
- Use a warmer contrasting fireworks palette rather than the product's base cool palette
