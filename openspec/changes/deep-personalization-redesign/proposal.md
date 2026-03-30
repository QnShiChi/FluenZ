## Why

The current Personalized Learning onboarding is a 4-step preset form that creates shallow, generic learning paths. Three critical issues make it insufficient:

1. **Level bug**: The frontend collects `selectedLevel` but never sends it in the API payload — the backend silently falls back to `user.currentLevel` (always BEGINNER from registration), making level selection completely non-functional.
2. **Shallow profiling**: Profession is a fixed dropdown of 10 items, contexts are 8 hardcoded chips, goals are an optional free-text afterthought — none of this captures the learner's real pain points, industry, seniority, or communication partners.
3. **Tiny output**: The LLM prompt requests 3-5 topics with 2-4 situations each at `max_tokens=8192`, producing paths too small for sustained learning. The system needs 20-25 topics with strict structure (5 chunks × 3 phrases each) to feel like a real learning journey.

## What Changes

- **[BREAKING] New onboarding payload schema**: Replace `OnboardingRequest` (professionId, contexts, goals) with a rich `PersonalizedOnboardingRequest` containing jobRole, industry, seniority, communicateWith, communicationChannels, painPoints, goals, level, and custom inputs
- **New frontend onboarding flow**: Replace `OnboardingPage.tsx` with a 7-step AI-guided onboarding (role → industry → seniority/goals → communication → pain points → goals → review+persona preview)
- **Onboarding config system**: Centralized TypeScript config for all preset choices (roles, industries, channels, pain points, goals) — no hardcoded UI lists
- **Learner profile persistence**: New `LearnerProfile` entity storing raw onboarding payload (JSONB) + normalized fields for analytics, regeneration, and debugging
- **Fix level bug end-to-end**: Frontend sends level → backend validates → prompt uses it → generation respects it
- **2-pass generation pipeline**: Pass 1 generates blueprint (persona + 20-25 topic roadmap), Pass 2 generates detailed content per topic batch (5 chunks × 3 phrases each)
- **Progress UI during generation**: Real-time progress bar showing "Generating topic 5/25..." via SSE or polling
- **AI persona preview**: Lightweight LLM call on the review step to generate a short learner persona summary before generation, with rule-based fallback
- **Increased token budget**: Raise `max_tokens` significantly and use batched generation to handle 20-25 topics reliably

## Capabilities

### New Capabilities
- `deep-onboarding-flow`: Multi-step AI-guided onboarding with progressive profiling, conditional steps, search, custom input, and live summary
- `learner-profile-persistence`: Store raw onboarding payload + normalized learner profile in DB for regeneration, analytics, and long-term personalization
- `two-pass-generation`: Blueprint-first generation pipeline producing 20-25 topics with strict content contracts (5 chunks, 3 phrases each)

### Modified Capabilities
- None (existing specs are unaffected at the requirement level)

## Impact

**Backend**:
- `OnboardingRequest.java` — replaced with new rich DTO
- `OnboardingService.java` — refactored for new payload, level fix, 2-pass pipeline, profile persistence
- `LlmService.java` — new prompts for blueprint + detail passes, increased token limits, batch processing
- `OnboardingController.java` — new endpoint for persona preview, progress streaming
- New entity: `LearnerProfile` with JSONB payload column
- New DTO: `PersonalizedOnboardingRequest`, `PersonaPreviewRequest/Response`, `GenerationProgressResponse`

**Frontend**:
- `OnboardingPage.tsx` — complete rewrite with 7-step flow
- New: `onboardingConfig.ts` — centralized preset data
- New: `useOnboardingStore.ts` — state management for multi-step flow
- New: `PersonaPreview` component for AI-generated summary

**API**:
- `POST /api/onboarding/generate` — new request schema (breaking for mobile clients if any)
- `POST /api/onboarding/persona-preview` — new endpoint
- `GET /api/onboarding/generation-progress/{pathId}` — new progress endpoint

**Database**:
- New table: `learner_profiles` (auto-created by Hibernate ddl-auto)
- `learning_paths` table: new `learner_profile_id` FK column
