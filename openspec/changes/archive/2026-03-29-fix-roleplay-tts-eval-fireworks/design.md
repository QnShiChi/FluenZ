## Overview

This change corrects regressions introduced during the premium roleplay UI redesign and replaces the unreliable custom completion celebration with a library-backed implementation.

The work spans both frontend and backend:

- frontend fixes roleplay TTS sequencing and swaps the completion animation implementation
- backend fixes the roleplay LLM prompt so the final turn answers first, evaluates second, and never requests emoji

## Goals

- Restore the missing English spoken guidance in the AI roleplay flow
- Preserve the intended two-turn roleplay contract:
  - turn 1: AI acknowledges the learner's answer
  - turn 2: AI answers the learner's question, then evaluates
- Remove emoji from evaluation output at the prompt and UI levels
- Replace the custom fireworks effect with `@fireworks-js/react`

## Non-Goals

- No redesign of the broader premium roleplay layout
- No change to practice completion persistence semantics
- No backend streaming or protocol changes
- No rewrite of the practice state machine outside roleplay sequencing

## Current Problems

### 1. Missing Roleplay Guidance TTS

The old roleplay flow explicitly played two English cues:

- opening instruction equivalent to "Please answer the following question."
- handoff cue equivalent to "Now it's your turn to ask."

After the UI redesign, those cues are no longer being spoken, so the learner loses auditory guidance at two important transitions.

### 2. Broken Final Turn Behavior

The backend roleplay contract expects turn 2 to:

1. answer the learner's question in English
2. add `---`
3. provide evaluation in Vietnamese

However, the current result is jumping straight to evaluation. This indicates the final-turn prompt instructions are not being enforced clearly enough, or the frontend sequencing no longer aligns with the expected handoff.

### 3. Emoji Still Present in Evaluation

Emoji are still appearing because the backend prompt explicitly instructs the model to use them and includes emoji-heavy examples. This must be corrected at the source.

### 4. Completion Fireworks Are Not Reliable Enough

The custom CSS-based celebration is not producing the intended effect. A well-supported library implementation is a better fit for this celebratory moment.

## Proposed Design

### 1. Restore Spoken Guidance Around Roleplay Turns

Frontend should explicitly reintroduce two English TTS cues:

- before the first AI question: `Please answer the following question.`
- after the first AI acknowledgment: `Now it's your turn to ask.`

These cues should remain outside the visible chat transcript so the conversation surface stays clean.

#### Sequencing

1. Play opening cue in English
2. Stream/render the first AI question
3. Speak the first AI question
4. Wait for learner response
5. Receive AI acknowledgment for turn 1
6. Stream/render the acknowledgment
7. Speak the acknowledgment
8. Play handoff cue in English
9. Wait for learner question
10. Receive final AI answer plus evaluation

### 2. Enforce Correct Final-Turn Prompt Contract

The backend roleplay prompt in `RoleplayService` should explicitly instruct the final turn to:

- answer the learner's last question first in plain English
- keep that answer concise and natural
- then append `---`
- then produce an emoji-free Vietnamese evaluation block

The example response must also be rewritten without emoji, because example formatting strongly influences model behavior.

### 3. Remove Emoji at the Source and in Presentation

Emoji should be removed from:

- backend prompt instructions
- backend prompt example output
- any frontend copy or helper text in the affected roleplay/completion surfaces

This keeps the interaction aligned with the premium academic tone already chosen.

### 4. Replace Custom Celebration with `@fireworks-js/react`

Instead of the custom CSS fireworks overlay, the completion screen should render a dedicated React fireworks component using `@fireworks-js/react`.

#### Desired Behavior

- launches from the lower area of the completion card or viewport
- runs once for about 2-3 seconds
- does not loop forever
- remains visually behind the primary completion content
- can be tuned via library options rather than handcrafted CSS particles

#### Integration Strategy

- add `@fireworks-js/react` dependency
- mount the fireworks component only when the completion screen is entered
- stop/unmount it after the short celebration window
- keep reduced-motion fallback behavior

## State and Flow Notes

### Frontend Roleplay

- keep `Speak` / `Type` mode logic
- keep frontend-only AI text streaming
- restore guidance TTS as separate audio events
- ensure status text aligns with the real turn state

### Backend Roleplay

- maintain existing `/practice/roleplay/chat` endpoint
- keep `turnNumber` semantics
- tighten prompt wording for turn 2 behavior

### Completion

- keep existing `sessionComplete` signal
- only change the visual celebration implementation

## Risks

- If the library fireworks container is layered incorrectly, it can obscure content or appear invisible
- If the handoff cue plays at the wrong time, the learner may feel rushed or confused
- If the prompt is not specific enough, the model may still compress answer and evaluation into an awkward format

## Resolved Decisions

- Use `@fireworks-js/react`
- Keep the premium roleplay UI already introduced
- Fix emoji at the backend prompt level, not just by post-processing
