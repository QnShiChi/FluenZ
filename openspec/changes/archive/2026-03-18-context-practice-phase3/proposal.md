## Why

Phase 1 (Component Isolation) and Phase 2 (Combination) train users to recognize and pronounce phrases with visual prompts. But in real conversations, there are no text prompts — someone asks a question and you must respond reflexively. Phase 3 bridges this gap by training contextual reflex: hear a question → produce the answer from memory. Adding context images per chunk further strengthens visual-associative memory.

## What Changes

- **New step type `CONTEXT_SPEAK`**: User hears context question via TTS, sees only the question text + a context image, no answer hints. Must respond from memory.
- **Context images via Unsplash API**: LLM generates `imageKeyword` per variable chunk at path creation time. Backend fetches stock image URL from Unsplash and stores it in DB.
- **Answer reveal after evaluation**: After speaking (any score), show the full correct answer on screen for reinforcement. Score ≥ 70 to continue.
- **Phase 3 steps appended to existing session**: 3 new `CONTEXT_SPEAK` steps (one per variable chunk) added after Phase 2 in `generateSteps()`.

## Capabilities

### New Capabilities
- `context-images`: Backend service to fetch stock images from Unsplash API based on LLM-generated keywords, store URLs in `SubPhrase.imageUrl`

### Modified Capabilities
- `practice-ui`: New `CONTEXT_SPEAK` step type in state machine and PracticePage rendering

## Impact

- **Backend**: New `ImageService` (Unsplash API client), LLM prompt adds `imageKeyword` field, `SubPhrase` entity gains `imageUrl` column, `OnboardingService` calls ImageService during path creation
- **Frontend**: New `CONTEXT_SPEAK` step type in `usePracticeStore.ts`, new rendering block in `PracticePage.tsx`
- **Data model**: `SubPhrase` gains `imageUrl` column. LLM `variableChunks` adds `imageKeyword` field.
- **Dependencies**: Unsplash API key (free tier: 50 req/hr)
