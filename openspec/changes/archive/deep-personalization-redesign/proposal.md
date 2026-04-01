## Why

The current Personalized Learning onboarding and generation pipeline creates shallow, generic, and slow learning paths. Several critical issues make it insufficient:

1. **Level bug**: The frontend collects `selectedLevel` but never sends it in the API payload — the backend silently falls back to `user.currentLevel` (always BEGINNER from registration), making level selection completely non-functional.
2. **Shallow profiling**: Profession is a fixed dropdown of 10 items, contexts are 8 hardcoded chips, goals are an optional free-text afterthought — none of this captures the learner's real pain points, industry, seniority, or communication partners.
3. **Tiny output**: The LLM prompt requests 3-5 topics with 2-4 situations each at `max_tokens=8192`, producing paths too small for sustained learning. The system needs 20-25 topics with strict structure (5 chunks × 3 phrases each) to feel like a real learning journey.
4. **Thumbnail quality failures**: situation thumbnails can be duplicated across a generated path, and some situations still end up with no thumbnail at all.
5. **Weak chunk learning units**: generated subphrases often collapse to single words, which breaks the intended "small usable phrase -> full sentence" learning model.
6. **Generation is too slow**: end-to-end personalized path generation can take 15-20 minutes, which is unacceptable for post-onboarding UX.

## What Changes

- **[BREAKING] New onboarding payload schema**: Replace `OnboardingRequest` (professionId, contexts, goals) with a rich `PersonalizedOnboardingRequest` containing jobRole, industry, seniority, communicateWith, communicationChannels, painPoints, goals, level, and custom inputs
- **New frontend onboarding flow**: Replace `OnboardingPage.tsx` with a 7-step AI-guided onboarding (role → industry → seniority/goals → communication → pain points → goals → review+persona preview)
- **Onboarding config system**: Centralized TypeScript config for all preset choices (roles, industries, channels, pain points, goals) — no hardcoded UI lists
- **Learner profile persistence**: New `LearnerProfile` entity storing raw onboarding payload (JSONB) + normalized fields for analytics, regeneration, and debugging
- **Fix level bug end-to-end**: Frontend sends level → backend validates → prompt uses it → generation respects it
- **2-pass generation pipeline**: Pass 1 generates blueprint (persona + topic roadmap), Pass 2 generates detailed content per situation/topic batch with strict chunk and subphrase contracts
- **Async thumbnail hydration pipeline**: Generate a usable text path first, then fetch/hydrate thumbnails asynchronously so users can enter the dashboard quickly while assets continue to fill in
- **Guaranteed thumbnail coverage**: Introduce multi-tier thumbnail fallback and curated internal fallback pool so every situation ends in a non-null image state
- **Thumbnail dedupe**: Add per-user-path duplicate prevention using source URL registry, image fingerprint metadata, fallback query retries, and curated pool rotation
- **Subphrase quality enforcement**: Strengthen prompts and add validation so subphrases are usable 2-6 word phrases by default, not isolated single tokens
- **Selective validation + retry**: Validate only failing situations/chunks/subphrases/assets and retry narrowly instead of regenerating the whole path
- **Progress UI during generation**: Real-time progress bar showing "Generating topic 5/25..." via SSE or polling
- **AI persona preview**: Lightweight LLM call on the review step to generate a short learner persona summary before generation, with rule-based fallback
- **Performance target**: The first usable text path MUST appear within 2-3 minutes after the user clicks `Generate My Path`, while thumbnails finish hydrating shortly after in the background
- **Pipeline metadata and debugability**: Add generation status, content validation status, thumbnail queries, fallback queries, image source, retry counts, and dedupe markers so the pipeline is inspectable and retryable

## Capabilities

### New Capabilities
- `deep-onboarding-flow`: Multi-step AI-guided onboarding with progressive profiling, conditional steps, search, custom input, and live summary
- `learner-profile-persistence`: Store raw onboarding payload + normalized learner profile in DB for regeneration, analytics, and long-term personalization
- `two-pass-generation`: Blueprint-first generation pipeline producing 20-25 topics with strict content contracts (5 chunks, 3 phrases each)
- `thumbnail-hydration-pipeline`: Async external-first thumbnail fetching with dedupe, retries, fallback query expansion, and guaranteed final fallback coverage
- `generation-content-validation`: Post-generation validation for subphrase quality and targeted regeneration of invalid chunks/situations
- `personalized-generation-performance`: SLA-oriented orchestration for returning usable text content within 2-3 minutes

### Modified Capabilities
- None (existing specs are unaffected at the requirement level)

## Impact

**Backend**:
- `OnboardingRequest.java` — replaced with new rich DTO
- `OnboardingService.java` — refactored for new payload, level fix, 2-pass pipeline, profile persistence
- `LlmService.java` — new prompts for blueprint + detail passes, increased token limits, stricter structured output, phrase-quality contract
- `OnboardingController.java` — new endpoint for persona preview, progress streaming
- New entity: `LearnerProfile` with JSONB payload column
- New DTOs / metadata for generation state, thumbnail hydration, validation status, and retry tracking
- Image pipeline services for dedupe, fallback strategy, and async hydration
- Optional curated fallback thumbnail registry/pool

**Frontend**:
- `OnboardingPage.tsx` — complete rewrite with 7-step flow
- New: `onboardingConfig.ts` — centralized preset data
- New: `useOnboardingStore.ts` — state management for multi-step flow
- New: `PersonaPreview` component for AI-generated summary
- Dashboard/generation UX updated to handle usable text-first paths with thumbnails hydrating asynchronously

**API**:
- `POST /api/onboarding/generate` — new request schema (breaking for mobile clients if any)
- `POST /api/onboarding/persona-preview` — new endpoint
- `GET /api/onboarding/generation-progress/{pathId}` — new progress endpoint

**Database**:
- New table: `learner_profiles` (auto-created by Hibernate ddl-auto)
- `learning_paths` table: new `learner_profile_id` FK column
- Additional generation/thumbnail metadata fields or companion tables for content validation status, thumbnail query lineage, dedupe markers, retry counters, and generation state
