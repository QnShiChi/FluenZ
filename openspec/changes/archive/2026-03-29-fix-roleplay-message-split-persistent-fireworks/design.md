## Overview

This change refines the already-updated roleplay and completion flow in three concrete ways:

1. make the opening English guidance cue reliably audible when the learner enters roleplay
2. split the final AI answer and the Vietnamese evaluation into two separate rendered chat blocks
3. keep completion fireworks active until the learner explicitly exits the completion screen

The goal is not to redesign the experience again, but to correct the remaining interaction details so the flow feels clearer and more intentional.

## Goals

- Ensure the learner always hears the opening English roleplay cue
- Improve transcript readability by separating answer content from evaluation content
- Avoid forcing TTS on the Vietnamese evaluation block when it does not sound natural
- Keep the celebratory completion state alive until the learner actively leaves it

## Non-Goals

- No redesign of the premium roleplay layout itself
- No changes to practice scoring or persistence semantics
- No additional backend streaming work
- No new completion screen route or navigation model

## Current Problems

### 1. Opening Cue Is Not Reliably Spoken

Even after the previous fix attempt, the learner can still land in roleplay without hearing the explicit English opening guidance cue. This weakens the transition into the conversation.

### 2. Final AI Turn Is Still Hard to Scan

The AI's final English answer and the Vietnamese evaluation are still visually appearing as one combined block. Even if the backend includes a separator, the UI is not treating them as two distinct chat messages.

### 3. Evaluation TTS Is Not Always Desirable

The Vietnamese evaluation block is instructional and may not sound natural or useful when auto-read aloud. The learner mainly needs the spoken English answer, not necessarily the spoken evaluation.

### 4. Fireworks Stop Too Early

The current completion celebration still behaves like a timed burst. The intended behavior is for the celebration to remain active until the learner chooses to finish or leave the completion screen.

## Proposed Design

### 1. Reliable Opening Guidance Cue

The roleplay flow should explicitly sequence the opening cue as a separate English TTS event before the first AI prompt is spoken.

#### Required behavior

1. learner enters roleplay
2. system speaks `Please answer the following question.`
3. system renders and speaks the first AI prompt
4. learner responds

The cue should remain outside the transcript and should not depend on the learner pressing any replay control.

### 2. Final AI Response Split Into Two Messages

The frontend should parse the final AI payload into:

- `answerMessage`: English response before `---`
- `evaluationMessage`: Vietnamese guidance after `---`

These should render as two separate AI coach blocks in the transcript, in order:

1. English answer bubble
2. Vietnamese evaluation bubble

This makes the chat easier to read and reinforces that the AI first participates in the conversation, then steps into tutor feedback mode.

### 3. TTS Policy for Final Turn

TTS should prioritize the English answer bubble.

The Vietnamese evaluation bubble:

- may render silently by default
- should not block progression if no TTS is played
- may optionally support manual replay later, but that is outside this change

### 4. Persistent Fireworks Lifecycle

The completion fireworks should remain active while the learner stays on the completion screen.

They should stop only when the learner:

- presses the explicit completion action
- navigates back to the dashboard
- or otherwise leaves/unmounts the screen

This means the fireworks component lifecycle should be tied to screen presence instead of a short timeout.

## Backend / Frontend Contract

The backend should continue returning the final answer and evaluation separated by `---`.

Frontend responsibility:

- parse the response into two parts
- render two message blocks
- speak only the English answer unless evaluation TTS is explicitly needed later

If the separator is missing unexpectedly:

- render the whole response as a single AI message
- avoid crashing the roleplay flow

## State and Flow Notes

### Roleplay

- keep `Speak` / `Type` mode
- keep voice-first interaction
- keep frontend-only streaming reveal for AI messages
- apply streaming separately to the final answer block and evaluation block if both exist

### Completion

- keep `@fireworks-js/react`
- stop fireworks on unmount or exit action
- do not auto-stop after a fixed 2-3 second window in this change

## Risks

- If the response parsing is too naive, unusual model output may produce awkward splits
- Persistent fireworks can become distracting if intensity is not tuned down
- If opening cue and first AI prompt sequencing overlap, audio can feel cluttered

## Resolved Decisions

- Opening cue must be spoken
- Final answer and evaluation must render as separate blocks
- Vietnamese evaluation does not need automatic TTS
- Fireworks continue until the learner exits the completion screen
