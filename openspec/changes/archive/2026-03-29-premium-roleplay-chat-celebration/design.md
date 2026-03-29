## Overview

This change refines the last emotional mile of the practice experience:

1. The AI roleplay step becomes a premium coach conversation surface instead of a visibly phased exercise block.
2. The completion screen gains a short celebratory motion sequence that rewards the learner immediately after the roleplay phase ends.

The redesign remains frontend-first. Backend chat APIs and completion semantics stay unchanged for this phase.

## Goals

- Make the roleplay step feel like a real-time tutoring chat.
- Keep speaking as the primary action while still offering an intentional text fallback.
- Simulate AI streaming locally after the full response arrives from the backend.
- Preserve automatic TTS playback for AI replies.
- Remove emoji and reduce overt "gamey" signaling.
- Add a short upward fireworks celebration when the learner lands on the completion screen.

## Non-Goals

- No backend token streaming or SSE/WebSocket work.
- No change to practice scoring rules or completion persistence payloads.
- No redesign of earlier quiz/speaking steps outside the roleplay segment and completion moment.

## Current State

### Roleplay

`RoleplayChatUI.tsx` currently uses an internal phase machine with system/AI/user message types. The UI still exposes exercise-state feeling through:

- top labels and helper messages that read like a training wizard
- system notices mixed into the conversation surface
- AI replies appearing as full static bubbles
- a footer that behaves like a control area rather than a chat composer
- emoji in labels and states

### Completion

`PracticePage.tsx` currently shows a static completion summary with score metrics and failed words. It lacks a moment of arrival or visual payoff when the learner finishes the roleplay phase.

## Proposed Design

### 1. Premium Coach Roleplay Surface

Replace the current roleplay block with a conversation layout that includes:

- a restrained conversation header
- a scrollable transcript area
- distinct AI and learner bubble treatments
- a bottom composer/action dock
- a mode switch for `Speak` and `Type`

The page should visually communicate "conversation with a tutor" rather than "step 5 of a guided task".

#### Layout Principles

- Clean, bright, premium surfaces
- Minimal accent usage
- No emoji
- AI messages feel calm, authoritative, and readable
- Learner actions remain obvious without overpowering the transcript

### 2. Voice-First, Optional Type Mode

Speaking remains the default mode. The composer area should open in `Speak` mode with the microphone action emphasized.

Typing is available only after the learner explicitly switches modes. In `Type` mode:

- a text input field becomes visible
- send action becomes available
- microphone prominence reduces

Switching back to `Speak` restores the voice-first composer.

This preserves product intent: learners should practice speaking unless they consciously opt out.

### 3. Frontend-Only Streaming Reveal

When the backend returns a full AI response:

1. The UI inserts a pending AI bubble.
2. The response is revealed progressively in the bubble through a timed frontend render effect.
3. TTS begins in sync with the displayed AI message lifecycle.

For this phase, "streaming" is a presentation effect only. There is no protocol change to backend chat endpoints.

#### Streaming Behavior

- Learner messages appear instantly.
- AI messages reveal progressively.
- While revealing, the AI bubble should feel alive but not flashy.
- A subtle typing indicator may appear before the reveal starts.
- If the user has reduced-motion preferences, the system should shorten or disable the streaming animation.

### 4. System Guidance Without Breaking Chat Immersion

System prompts such as "your turn" should no longer dominate the transcript as special decorative messages. Guidance should be delivered through:

- subtle inline status text near the composer
- small coach hints outside the main conversation thread when necessary

This keeps the chat history focused on the actual conversation between AI and learner.

### 5. Completion Celebration

When the learner enters the completion screen after the roleplay phase:

- trigger a one-time upward fireworks animation
- duration: roughly 2 to 3 seconds
- celebration should sit behind or around the content without blocking score readability

The animation should create a clean victory moment, not a party-like effect. It should feel polished and premium.

#### Motion Principles

- one-time only per completion screen entry
- no looping
- upward launch from the lower viewport area
- should not obscure score ring or CTA
- should respect reduced-motion settings with a toned-down fallback

## Component Strategy

Potential frontend decomposition:

- `RoleplayChatUI`
  - orchestrates conversation state
- `RoleplayTranscript`
  - scrollable message list
- `RoleplayMessageBubble`
  - AI vs learner bubble rendering
- `RoleplayComposer`
  - `Speak` / `Type` mode switch and input controls
- `StreamingText`
  - frontend timed text reveal
- `CompletionCelebration`
  - one-shot fireworks overlay/canvas layer

Exact filenames may vary, but the implementation should split large responsibilities instead of further growing one page-level file.

## State and Flow

### Roleplay

- Keep the existing backend call sequence and turn counts.
- Keep auto TTS for AI replies.
- Preserve existing mic/transcript integration for speaking mode.
- Add local `inputMode` state: `speak | type`
- Add local `draftMessage` state for type mode.
- Reuse the same backend chat endpoint whether the learner spoke or typed.

### Completion

- Do not change `sessionComplete` semantics in the practice store.
- When `sessionComplete` becomes true and the completion screen mounts, trigger celebration once.
- Do not replay celebration on incidental re-renders of the same mounted screen.

## Accessibility and UX Notes

- Reduced-motion users should see simplified streaming and celebration behavior.
- The roleplay surface should remain keyboard-usable in type mode.
- Status messaging should remain understandable without sound.
- TTS auto-play should not be removed, but UI should still offer replay controls.

## Risks

- Too much motion can make the premium UI feel noisy; animation must remain restrained.
- Streaming reveal that is too slow can frustrate users; pace needs tuning.
- Mixing TTS and streaming requires sequencing so the message feels coherent rather than delayed.
- Adding type mode can accidentally weaken speaking-first behavior if the switch is too prominent.

## Open Decisions Already Resolved

- Speaking remains primary; typing is secondary behind an explicit switch.
- AI streaming is frontend simulated after full response receipt.
- Auto TTS stays enabled.
- Fireworks trigger once, for about 2-3 seconds, on completion screen entry.
- Visual direction is premium tutor/coach, clean and academically polished.
