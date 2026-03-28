## Why

Two UX issues are breaking the learning flow:
1. In speaking steps, the result area appears too far below the microphone/action area, so users must scroll down to inspect feedback and then scroll back up to retry.
2. The current visual treatment relies too much on glow, gradient, and transparent surfaces, which makes the interface feel noisy and visually tiring.

## What Changes

- Redesign speaking steps into a fixed practice card that keeps the prompt, media, mic/actions, and result states inside the same visible learning block.
- Make the result panel appear inline in the same practice card after evaluation instead of pushing feedback far below the user’s interaction point.
- Simplify the learning shell visual style by reducing heavy gradients, glow, and translucent layers in favor of cleaner solid surfaces and restrained accents.

## Capabilities

### Modified Capabilities
- `practice-ui`: fixed in-viewport speaking card with inline result states
- `frontend-scaffolding`: simplified visual language for learning surfaces

## Impact

- **Frontend**: `PracticePage.tsx`, shared learning shell/surfaces, theme tokens and card styling
- **UX**: fewer scroll interruptions, clearer state transitions, calmer and more focused interface
