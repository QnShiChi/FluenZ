## Phase 1: Level Bug Fix & Backend Foundation

- [ ] 1.1 Add `level` field to `OnboardingRequest.java` with `@NotNull` validation
- [ ] 1.2 Update `OnboardingService.generatePath()` to use `request.getLevel()` instead of `user.getCurrentLevel()`
- [ ] 1.3 Update `OnboardingService.generatePath()` to also set `user.setCurrentLevel(request.getLevel())` to keep user profile in sync
- [ ] 1.4 Update frontend `OnboardingPage.tsx` to include `level: selectedLevel` in the submit payload
- [ ] 1.5 Add trace logging: log level at each stage (FE console → API payload → backend parse → prompt input)
- [ ] 1.6 Verify level flows end-to-end: select Intermediate → check backend log → check LLM prompt → check generated content difficulty

## Phase 2: Learner Profile Persistence

- [ ] 2.1 Create `LearnerProfile` entity with fields: `id`, `rawPayload` (TEXT/JSON column), `jobRole`, `industry`, `seniority`, `level`, `personaSummary`, timestamps
- [ ] 2.2 Create `LearnerProfileRepository`
- [ ] 2.3 Add `learnerProfile` FK to `LearningPath` entity (`@ManyToOne`, nullable for backwards compatibility)
- [ ] 2.4 Update `OnboardingService` to create and persist `LearnerProfile` from the new onboarding payload before path generation

## Phase 3: Generation Metadata & Status Tracking

- [ ] 3.1 Add generation status metadata for personalized generation phases such as `BLUEPRINT`, `DETAIL_CONTENT`, `PUBLISHED_TEXT`, `THUMBNAIL_HYDRATION`, `COMPLETE`, `FAILED`
- [ ] 3.2 Add thumbnail lineage metadata: `thumbnailQuery`, `fallbackQuery`, `thumbnailSource`, `imageFingerprint`, `assetRetryCount`
- [ ] 3.3 Add content validation metadata such as `contentValidationStatus`
- [ ] 3.4 Decide whether these fields live directly on entities or in dedicated companion tables/status records

## Phase 4: New Onboarding Request Schema

- [ ] 3.1 Create `PersonalizedOnboardingRequest` DTO with fields: `jobRole`, `industry`, `seniority`, `communicateWith[]`, `communicationChannels[]`, `communicationContexts[]`, `painPoints[]`, `goals[]`, `customGoal`, `customContext`, `level`
- [ ] 3.2 Update `OnboardingController.generate()` to accept `PersonalizedOnboardingRequest`
- [ ] 3.3 Update `OnboardingService.generatePath()` to work with new DTO (remove `professionId` dependency, use free-text `jobRole` instead)
- [ ] 3.4 Decide: keep `Profession` entity FK on LearningPath or replace with learner profile reference. (Likely: make FK nullable, store jobRole in profile)

## Phase 5: Phased Content Generation Pipeline

- [ ] 5.1 Create `LlmService.generateBlueprint()` — Phase A: takes full onboarding payload, returns persona summary + roadmap blueprint
- [ ] 4.2 Create blueprint prompt: instruct LLM to generate learner persona + prioritized communication needs + topic roadmap with difficulty progression
- [ ] 5.3 Create `LlmService.generateSituationBatch()` or equivalent detail generator that returns full situation/chunk content under a strict contract
- [ ] 5.4 Create batch detail prompt: include persona context, enforce each situation has exactly 5 chunks, each chunk has exactly 3 usable subphrases
- [ ] 5.5 Increase `max_tokens` to support larger structured outputs without forcing oversized single calls
- [ ] 5.6 Increase request timeouts for generation calls as needed, but keep orchestration optimized for 2-3 minute text-path SLA
- [ ] 5.7 Implement batch/parallel detail orchestration in `OnboardingService`
- [ ] 5.8 Publish/persist the first usable text path as soon as text content passes validation, before thumbnails finish hydrating
- [ ] 5.9 Add targeted retry logic for failed batches or invalid content units instead of regenerating the whole path
- [ ] 5.10 Create or update DTOs for blueprint and detail phases

## Phase 6: Subphrase Quality Rules & Validation

- [ ] 6.1 Update prompts so subphrases are generated as usable communicative phrases, not isolated tokens
- [ ] 6.2 Add validation rule: reject one-word subphrases by default unless explicitly allowed as a special case
- [ ] 6.3 Add validation rule: default subphrase length target is 2-6 words
- [ ] 6.4 Add validation rule: 3 subphrases in a chunk must meaningfully support building the root sentence
- [ ] 6.5 Regenerate only the failing chunk/subphrases when phrase-quality validation fails

## Phase 7: Thumbnail Hydration, Dedupe, And Fallback

- [ ] 7.1 Design external-first thumbnail pipeline with 4 fallback tiers: exact query -> broader semantic query -> generic professional query -> curated internal pool
- [ ] 7.2 Add per-user-path used-image registry to prevent obvious duplicate thumbnails within the same generated path
- [ ] 7.3 Add duplicate detection strategy using source URL checks plus image fingerprint/hash marker where feasible
- [ ] 7.4 Add fallback query generation rules when the first result is duplicated, missing, broken, or poor quality
- [ ] 7.5 Add bounded retry logic for switching query and/or image source candidate
- [ ] 7.6 Guarantee every situation eventually receives a non-null thumbnail
- [ ] 7.7 Add curated local/shared fallback thumbnail pool grouped by categories such as meeting, office, presentation, discussion, laptop, and professional portrait
- [ ] 7.8 Ensure the UI never ends in a broken or empty image state for finalized personalized paths

## Phase 8: Generation Progress Tracking

- [ ] 8.1 Create `GenerationProgressService` with `ConcurrentHashMap<UUID, GenerationProgress>` or equivalent in-memory progress state
- [ ] 8.2 Create `GenerationProgress` DTO with phases including `BLUEPRINT`, `DETAIL_CONTENT`, `PUBLISHED_TEXT`, `THUMBNAIL_HYDRATION`, `COMPLETE`, `FAILED`
- [ ] 8.3 Create `GET /api/onboarding/generation-progress/{pathId}` endpoint
- [ ] 8.4 Update generation pipeline to report progress after blueprint completion, detail batches, text publish, and thumbnail hydration
- [ ] 8.5 Clear ephemeral progress state after completion (or after TTL), while keeping durable metadata for debugging/recovery

## Phase 9: AI Persona Preview

- [ ] 6.1 Create `POST /api/onboarding/persona-preview` endpoint accepting the onboarding payload
- [ ] 6.2 Create lightweight LLM prompt for persona summary (3-4 sentences + key communication priorities)
- [ ] 6.3 Implement 5-second timeout with rule-based fallback (compose summary from payload fields)
- [ ] 6.4 Create `PersonaPreviewResponse` DTO: `personaSummary`, `communicationPriorities[]`, `isAiGenerated` (flag)

## Phase 10: Frontend — Onboarding Config

- [ ] 7.1 Create `onboardingConfig.ts` with typed exports for: roles (professional + student/career-change categories), industries, seniority levels, communication targets, communication channels, pain points (grouped by type), goal presets
- [ ] 7.2 Each option: `{ value: string, label: string, icon?: string, category?: string }`
- [ ] 7.3 Export helper functions: `getRoleCategory(role)` → 'professional' | 'student' | 'career-change'

## Phase 11: Frontend — New Onboarding Page

- [ ] 8.1 Create `useOnboardingStore.ts` (Zustand): multi-step state, all field values, computed `canNext`, `isComplete`
- [ ] 8.2 Rewrite `OnboardingPage.tsx` with 7-step layout: progress bar + step title/description + content area + live summary sidebar
- [ ] 8.3 Step 1 — Job Role: searchable grid with categories (Professional / Student / Other), custom input
- [ ] 8.4 Step 2 — Industry: searchable grid, custom input
- [ ] 8.5 Step 3 — Conditional: Seniority selector (for professional) OR goal-oriented question (for student/career-change)
- [ ] 8.6 Step 4 — Communication: dual multi-select for "Who you communicate with" + "Communication channels"
- [ ] 8.7 Step 5 — Pain Points: multi-select chips grouped by category (vocabulary, grammar, pronunciation, confidence, specific situations)
- [ ] 8.8 Step 6 — Goals: preset goal chips + custom goal text input
- [ ] 8.9 Step 7 — Review: summary of all selections + AI persona preview + level selector + "Generate" button
- [ ] 8.10 Generation loading state: progress bar with phase-aware messaging and usable-path readiness updates, polling every 2 seconds
- [ ] 8.11 Ensure the UI can move forward once the usable text path is ready, even while thumbnails continue hydrating
- [ ] 8.12 Ensure the new page fully replaces current `OnboardingPage.tsx` for Personalized flow

## Phase 12: Performance Optimization

- [ ] 12.1 Audit current latency bottlenecks across LLM calls, orchestration, validation, image fetch, and retries
- [ ] 12.2 Parallelize independent detail-generation work where safe
- [ ] 12.3 Decouple thumbnail hydration from text-path readiness
- [ ] 12.4 Reduce retry waste by retrying only failed batches/items/assets
- [ ] 12.5 Cache reusable/generic thumbnail candidates where appropriate
- [ ] 12.6 Ensure the first usable text path appears within 2-3 minutes after `Generate My Path`

## Phase 13: Styling & Polish

- [ ] 9.1 Premium UI: empathetic step titles, gradient accents, smooth transitions between steps
- [ ] 9.2 Live summary panel: compact sidebar or floating card showing all selections, updating in real-time
- [ ] 9.3 Persona preview card: glassmorphism card with AI-generated summary, loading skeleton, fallback state
- [ ] 9.4 Search functionality: debounced search filter for role and industry grids
- [ ] 9.5 Mobile responsive: stack summary panel below content on small screens

## Phase 14: Verification

- [ ] 14.1 End-to-end: complete new onboarding → verify LearnerProfile and generation metadata persist correctly
- [ ] 14.2 Level fix: select each level → verify in backend logs → verify in generated content
- [ ] 14.3 Content contract: verify each situation has exactly 5 chunks and each chunk has exactly 3 usable subphrases
- [ ] 14.4 Subphrase quality: verify no obviously invalid one-word filler subphrases remain
- [ ] 14.5 Usable-path SLA: verify text path becomes available within 2-3 minutes after `Generate My Path`
- [ ] 14.6 Thumbnail coverage: verify 100% of situations eventually receive thumbnails
- [ ] 14.7 Thumbnail dedupe: verify no obvious duplicate thumbnails within the same personalized path
- [ ] 14.8 Progress tracking: verify progress endpoint reflects text-ready vs thumbnail-hydration phases
- [ ] 14.9 Persona preview: verify AI preview displays, then test fallback by temporarily breaking LLM call
- [ ] 14.10 Backward compatibility: verify Default catalog learning mode still works
