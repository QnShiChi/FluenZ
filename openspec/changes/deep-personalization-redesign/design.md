## Context

FluenZ currently has a 4-step onboarding flow (profession → level → contexts → goals) that feeds a single LLM call to generate a 3-5 topic learning path. The system uses preset dropdown lists, and a critical bug causes the selected English level to be silently ignored.

**Current architecture**: `OnboardingPage.tsx` → `POST /api/onboarding/generate` with `{professionId, contexts, goals}` → `OnboardingService.generatePath()` → single `LlmService.generateLearningPath()` call → save entities → async image population.

**Key constraints**: Hibernate `ddl-auto:update` (no migrations needed), OpenRouter/Gemini LLM with per-call token limits, existing entity hierarchy (LearningPath → Topic → Situation → Chunk → SubPhrase), async image population system already working.

## Goals / Non-Goals

**Goals:**
- Fix the level selection bug end-to-end (UI → payload → backend → prompt)
- Replace onboarding with 7-step progressive profiling capturing job role, industry, seniority, communication targets, channels, pain points, and goals
- Persist full onboarding payload + normalized learner profile in DB
- Generate AI persona preview on the review step (with rule-based fallback)
- Implement 2-pass generation: blueprint (persona + 20-25 topic roadmap) then batched detail generation
- Enforce strict content contract: 5 chunks/topic, 1 base sentence/chunk, exactly 3 phrases/chunk
- Show real-time generation progress to the user
- Centralize all preset choice data in a typed config file

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

### 4. 2-pass generation with batched detail calls

**Decision**:
- **Pass 1** (Blueprint): Single LLM call generating learner persona summary + 20-25 topic roadmap with titles, descriptions, and difficulty progression. Output: ~2000 tokens.
- **Pass 2** (Details): Batched LLM calls, 5 topics per batch, generating full chunk/phrase content. 4-5 calls × ~8000 tokens each. Each call includes the persona context from Pass 1.

**Why**: Single-call generation of 20-25 topics with full content exceeds reliable token limits (~100K+ tokens needed). Batching keeps each call within safe limits while maintaining persona coherence.

**Alternative considered**: Single massive call with very high max_tokens — rejected for reliability, timeout risk, and output quality degradation.

### 5. Generation progress: polling-based approach

**Decision**: Use polling (`GET /api/onboarding/generation-progress/{pathId}`) instead of SSE/WebSocket. Store progress state in-memory (`ConcurrentHashMap`) keyed by pathId.

**Why**: Simpler to implement, no infrastructure changes needed. Frontend polls every 2 seconds. State is ephemeral (cleared after completion) — no DB overhead.

**Alternative considered**: SSE — more elegant but adds complexity for a feature used once per user.

### 6. Centralized onboarding config: TypeScript constants file

**Decision**: All preset choices (roles, industries, channels, pain points, goals) in a single `onboardingConfig.ts` with typed exports. Each category has `{ value, label, icon?, description? }` format.

**Why**: Single source of truth, easy to iterate, type-safe, no API calls needed. Categories are organized by domain (e.g., pain points grouped by type).

### 7. Persona preview: LLM call with 5s timeout + fallback

**Decision**: `POST /api/onboarding/persona-preview` sends the full onboarding payload, LLM generates a 3-4 sentence persona summary. If LLM fails or takes >5s, the backend returns a rule-based fallback built from the payload fields.

**Why**: The preview adds significant user delight. The fallback ensures the UX never breaks.

### 8. ReadTimeout increase for batched generation

**Decision**: Increase `RestTemplate.readTimeout` from 60s to 120s for generation calls. Each batch call processes 5 topics with full content.

**Why**: 5 detailed topics with chunks and phrases can take 30-60s through OpenRouter.

## Risks / Trade-offs

- **[Risk] Generation time ~30-60s total** → Mitigation: Progress bar with topic-by-topic updates. User sees progress immediately after Pass 1 completes (~5s).
- **[Risk] Token cost increase ~5x per path** → Mitigation: Accepted by user. Cache blueprint for potential regeneration.
- **[Risk] Batch LLM call failure mid-generation** → Mitigation: Retry logic (3 attempts per batch). Partial paths saved — user can regenerate failed topics later.
- **[Risk] LLM output format inconsistency** → Mitigation: Strict JSON schema in prompt with explicit examples. Parse validation before saving.
- **[Risk] In-memory progress state lost on restart** → Mitigation: Acceptable — generation completes in <60s. User can check DB-saved results.
