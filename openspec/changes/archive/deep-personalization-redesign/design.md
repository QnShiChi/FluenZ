## Context

FluenZ currently has a 4-step onboarding flow (profession → level → contexts → goals) that feeds a single LLM call to generate a small learning path. The system already has asynchronous image population, but the overall personalization pipeline still has four major quality and performance failures:

1. situation thumbnails are duplicated across a path
2. some situations still have no thumbnail
3. subphrases are often too short and collapse to single words
4. full path generation takes far too long (15-20 minutes)

**Current architecture**: `OnboardingPage.tsx` → `POST /api/onboarding/generate` with shallow payload → `OnboardingService.generatePath()` → mostly synchronous generation and asset enrichment → save entities → UI waits too long for a usable result.

**Key constraints**:
- Hibernate `ddl-auto:update` (no manual migration pipeline needed)
- OpenRouter/Gemini LLM with token and latency limits
- entity hierarchy is fixed: `LearningPath -> Topic -> Situation -> Chunk -> SubPhrase`
- user accepts `content first, thumbnail hydrate async later`
- final state must still guarantee 100% thumbnail coverage with no obvious duplicate thumbnails within the same personalized path

## Goals / Non-Goals

**Goals:**
- Fix the level selection bug end-to-end (UI → payload → backend → prompt)
- Replace onboarding with 7-step progressive profiling capturing job role, industry, seniority, communication targets, channels, pain points, and goals
- Persist full onboarding payload + normalized learner profile in DB
- Generate AI persona preview on the review step (with rule-based fallback)
- Implement a faster phased generation pipeline: usable text first, thumbnail hydration async after
- Enforce strict content contract: each `situation` has exactly 5 chunks, each chunk has 1 base sentence and exactly 3 usable subphrases
- Guarantee that every situation ultimately has a thumbnail
- Prevent obvious thumbnail duplication within a single personalized path
- Add validation and narrow retry for weak subphrases and missing/duplicate thumbnails
- Show real-time generation progress to the user
- Centralize all preset choice data in a typed config file
- Return the first usable text path within 2-3 minutes after `Generate My Path`

**Non-Goals:**
- Admin UI for editing presets (use config file for now)
- Redesigning the practice flow or Dashboard UI
- Changing the existing Default catalog learning mode
- Real-time collaboration or social features
- Mobile app changes

## Decisions

### 1. Onboarding flow structure: 7 steps with conditional branching

**Decision**: 7-step flow: Role → Industry → Seniority (conditional) → Communication → Pain Points → Goals → Review + Persona Preview

**Why**: Breaks the profiling into focused, digestible steps. Each step has empathetic copy + quick choices + search + custom input. Seniority is shown only for professional roles (not students/career changers).

**Alternative considered**: Single-page form with all fields — rejected for poor UX and overwhelming users.

### 2. Level selection: explicit in payload, not derived from user profile

**Decision**: Include `level` as a required field in the new onboarding request payload. The backend validates and uses it directly in the prompt instead of reading `user.currentLevel`.

**Why**: The current bug exists because level is never transmitted in the API payload. The fix must be end-to-end: UI state → submit payload → backend parse → prompt input.

**Alternative considered**: Keep using `user.currentLevel` but update it during onboarding — rejected because the level applies to the path, not the user globally.

### 3. Learner profile: JSONB raw payload + normalized columns

**Decision**: New `LearnerProfile` entity with:
- `rawPayload` (JSONB/TEXT column storing full onboarding request as JSON)
- Normalized fields: `jobRole`, `industry`, `seniority`, `level`
- `personaSummary` (AI-generated text from preview step)
- FK on `LearningPath` → `LearnerProfile`

**Why**: Raw payload enables regeneration and debugging. Normalized fields enable analytics and queries. Both stored for different use cases.

### 4. Phased generation with usable-text-first delivery

**Decision**:
- **Phase A** (Blueprint): Single LLM call generating learner persona summary + roadmap skeleton.
- **Phase B** (Detail content): Batched or parallel detail generation that produces situations, chunks, root sentences, and subphrases under a stricter structured contract.
- **Phase C** (Usable path publish): Persist and return the first usable text path to the UI as soon as text content passes contract validation.
- **Phase D** (Async thumbnail hydration): Fetch thumbnails after text publish, using multi-tier fallback and dedupe.
- **Phase E** (Targeted repair): Retry only failed chunks/situations/assets instead of regenerating the whole path.

**Why**: The user prioritizes getting into the product quickly after clicking `Generate My Path`. Text content is the core learning asset; thumbnails are enrichment. The system should not block the entire onboarding completion on external image fetching.

**Alternative considered**: Waiting for every thumbnail before returning the path — rejected because it directly conflicts with the 2-3 minute SLA for usable text content.

### 5. Generation progress: polling-based approach

**Decision**: Use polling (`GET /api/onboarding/generation-progress/{pathId}`) instead of SSE/WebSocket. Track separate phases such as `BLUEPRINT`, `DETAIL_CONTENT`, `PUBLISHED_TEXT`, `THUMBNAIL_HYDRATION`, `COMPLETE`, and `FAILED`.

**Why**: Simpler to implement, no infrastructure changes needed. Frontend polls every 2 seconds. State is ephemeral (cleared after completion) — no DB overhead.

**Alternative considered**: SSE — more elegant but adds complexity for a feature used once per user.

### 6. Centralized onboarding config: TypeScript constants file

**Decision**: All preset choices (roles, industries, channels, pain points, goals) in a single `onboardingConfig.ts` with typed exports. Each category has `{ value, label, icon?, description? }` format.

**Why**: Single source of truth, easy to iterate, type-safe, no API calls needed. Categories are organized by domain (e.g., pain points grouped by type).

### 7. Persona preview: LLM call with 5s timeout + fallback

**Decision**: `POST /api/onboarding/persona-preview` sends the full onboarding payload, LLM generates a 3-4 sentence persona summary. If LLM fails or takes >5s, the backend returns a rule-based fallback built from the payload fields.

**Why**: The preview adds significant user delight. The fallback ensures the UX never breaks.

### 8. Thumbnail pipeline: external-first, per-path dedupe, guaranteed fallback

**Decision**:
- Use an external-first thumbnail pipeline with a 4-tier fallback chain:
  1. exact situation query
  2. broader semantic role/context query
  3. generic professional/work/communication query
  4. curated internal fallback pool
- Maintain per-user-path used-image registry to avoid obvious duplicates.
- Track source URL, thumbnail query lineage, fallback query, fingerprint/hash, selected source, retry count, and generation status.
- If the first candidate collides with an already-used image or is too similar, retry with a new query or a new source candidate.

**Why**: The product priority is "always have an image, and avoid obvious duplicates" rather than "always find the perfect semantic image."

### 9. Subphrase quality contract + validation

**Decision**:
- Subphrases should default to 2-6 words and represent reusable communicative phrases, not isolated tokens.
- One-word subphrases are rejected by validation except for narrowly justified special cases.
- Add post-generation validation and targeted regeneration for only the failing chunk/subphrases.

**Why**: The chunk system is built around "small phrase -> full sentence". Single-word outputs do not satisfy that instructional model.

### 10. Performance target and orchestration

**Decision**:
- The first usable text path must appear within 2-3 minutes after `Generate My Path`.
- Thumbnail hydration continues asynchronously after text publication.
- Avoid blocking UI on thumbnail completion.
- Parallelize independent LLM detail batches where feasible, and keep thumbnail fetching fully decoupled from text publishing.
- Reduce wasteful retries by validating narrowly and repairing only the failed units.

**Why**: 15-20 minutes is unacceptable. The system must optimize for fast entry into learning, with assets filling in after the path is already usable.

### 11. ReadTimeout increase for batched generation

**Decision**: Increase `RestTemplate.readTimeout` from 60s to 120s for generation calls. Each batch call processes 5 topics with full content.

**Why**: 5 detailed topics with chunks and phrases can take 30-60s through OpenRouter.

## Risks / Trade-offs

- **[Risk] External image APIs remain flaky** → Mitigation: external-first retries, broader fallback queries, curated internal pool as final guaranteed fallback
- **[Risk] More metadata fields increase implementation complexity** → Mitigation: accepted because debugability and retryability are critical to fixing duplicate/missing thumbnail issues
- **[Risk] Stricter subphrase validation may increase regeneration rate** → Mitigation: targeted retry only for failing chunks, not whole-path regeneration
- **[Risk] Thumbnail hydration async means path first appears with partial image state** → Mitigation: accepted by product; usable text speed is higher priority than fully enriched assets at first paint
- **[Risk] In-memory progress state lost on restart** → Mitigation: acceptable for phase state, but durable generation/thumbnail status metadata should still be persisted on entities or companion tables for recovery and debugging
