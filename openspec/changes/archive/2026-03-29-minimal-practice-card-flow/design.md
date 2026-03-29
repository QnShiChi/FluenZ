## Context

The practice experience should feel continuous: see the prompt, answer, receive feedback, and retry without losing visual context. The current layout separates these states vertically, which forces unnecessary scrolling. The user also prefers a more minimal and less flashy UI direction.

## Goals / Non-Goals

**Goals:**
- Keep question, image, microphone/actions, and result feedback within one stable practice card for speaking steps
- Ensure feedback appears immediately inside the same visible area after evaluation
- Keep retry actions close to the result so the user can continue without scrolling
- Reduce visual noise across the learning shell by replacing strong gradient/glow treatments with simpler solid surfaces and restrained accents

**Non-Goals:**
- Changing backend evaluation logic
- Changing the order of the learning flow
- Redesigning quiz steps into a new interaction model beyond surface-level alignment

## Decisions

### 1. Speaking states live in one shared practice card
**Choice**: All speaking-oriented steps (`SPEAK`, `FULL_SENTENCE`, `FILL_BLANK`, `CONTEXT_SPEAK`) should use the same card structure with three vertically ordered zones:
- prompt zone: question/phrase/image
- action zone: mic + supporting controls
- result zone: loading or feedback content

**Why**: A stable layout avoids the current “scroll down to check, scroll up to retry” break in attention.

### 2. Result stays inline, directly near the mic
**Choice**: After evaluation finishes, the result zone appears in the same practice card immediately below or adjacent to the action zone, without pushing the result outside the main viewport. Retry messaging and retry action remain in the same local area.

**Why**: The user should be able to evaluate the response and retry in one glance.

### 3. Result can replace or compress idle mic state, but mic stays nearby for retry
**Choice**: During `done` state, the result area becomes the primary focus inside the card, while the microphone remains close enough for immediate retry instead of moving to a distant section.

**Why**: Feedback should dominate attention after scoring, but retry must still feel one step away.

### 4. Learning shell visual style becomes quieter
**Choice**: Reduce strong background glows, large gradients, and glassmorphism-like transparency across learning surfaces. Prefer:
- flatter page backgrounds
- solid card surfaces
- subtle borders
- limited orange accents only for emphasis and action

**Why**: A calmer interface improves readability and reduces the “burning/glowy” feeling the user dislikes.

## Files Expected

| File | Change |
|---|---|
| `frontend/src/pages/PracticePage.tsx` | Refactor speaking layout into a stable practice card with inline result states |
| `frontend/src/components/layout/LearningShell.tsx` | Tone down shell visuals and surface styling |
| `frontend/src/index.css` | Adjust theme tokens toward a cleaner minimal visual system |

## Resolved Direction

- **Visual style**: Minimal, cleaner, less glow/gradient-heavy
- **Speaking layout**: One unified practice card for all speaking steps
- **Result behavior**: Inline in the same card, with mic/actions still close for retry
