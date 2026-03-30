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

## Phase 3: New Onboarding Request Schema

- [ ] 3.1 Create `PersonalizedOnboardingRequest` DTO with fields: `jobRole`, `industry`, `seniority`, `communicateWith[]`, `communicationChannels[]`, `communicationContexts[]`, `painPoints[]`, `goals[]`, `customGoal`, `customContext`, `level`
- [ ] 3.2 Update `OnboardingController.generate()` to accept `PersonalizedOnboardingRequest`
- [ ] 3.3 Update `OnboardingService.generatePath()` to work with new DTO (remove `professionId` dependency, use free-text `jobRole` instead)
- [ ] 3.4 Decide: keep `Profession` entity FK on LearningPath or replace with learner profile reference. (Likely: make FK nullable, store jobRole in profile)

## Phase 4: 2-Pass Generation Pipeline

- [ ] 4.1 Create `LlmService.generateBlueprint()` — Pass 1: takes full onboarding payload, returns persona summary + 20-25 topic roadmap (title, description, level, imageKeyword)
- [ ] 4.2 Create blueprint prompt: instruct LLM to generate learner persona + prioritized communication needs + topic roadmap with difficulty progression
- [ ] 4.3 Create `LlmService.generateTopicBatch()` — Pass 2: takes persona context + 5 topic outlines, returns full chunk/phrase content for each
- [ ] 4.4 Create batch detail prompt: include persona context, enforce 5 chunks/topic, 1 rootSentence/chunk, exactly 3 variableChunks/chunk
- [ ] 4.5 Increase `max_tokens` to 16384 for blueprint call and 16384 per batch call
- [ ] 4.6 Increase `RestTemplate.readTimeout` to 120 seconds
- [ ] 4.7 Implement batch loop in `OnboardingService`: call Pass 1 → loop Pass 2 in batches of 5 → aggregate results
- [ ] 4.8 Add retry logic (3 attempts per batch call) with partial save support
- [ ] 4.9 Create blueprint DTOs: `LlmBlueprint`, `LlmBlueprintTopic`
- [ ] 4.10 Create batch detail DTOs matching existing entity structure

## Phase 5: Generation Progress Tracking

- [ ] 5.1 Create `GenerationProgressService` with `ConcurrentHashMap<UUID, GenerationProgress>` for in-memory state
- [ ] 5.2 Create `GenerationProgress` DTO: `phase` (BLUEPRINT/DETAILS/COMPLETE/FAILED), `currentBatch`, `totalBatches`, `currentTopicName`, `isComplete`
- [ ] 5.3 Create `GET /api/onboarding/generation-progress/{pathId}` endpoint
- [ ] 5.4 Update generation pipeline to report progress after each batch completes
- [ ] 5.5 Clear progress state after generation completes (or after 5 minutes TTL)

## Phase 6: AI Persona Preview

- [ ] 6.1 Create `POST /api/onboarding/persona-preview` endpoint accepting the onboarding payload
- [ ] 6.2 Create lightweight LLM prompt for persona summary (3-4 sentences + key communication priorities)
- [ ] 6.3 Implement 5-second timeout with rule-based fallback (compose summary from payload fields)
- [ ] 6.4 Create `PersonaPreviewResponse` DTO: `personaSummary`, `communicationPriorities[]`, `isAiGenerated` (flag)

## Phase 7: Frontend — Onboarding Config

- [ ] 7.1 Create `onboardingConfig.ts` with typed exports for: roles (professional + student/career-change categories), industries, seniority levels, communication targets, communication channels, pain points (grouped by type), goal presets
- [ ] 7.2 Each option: `{ value: string, label: string, icon?: string, category?: string }`
- [ ] 7.3 Export helper functions: `getRoleCategory(role)` → 'professional' | 'student' | 'career-change'

## Phase 8: Frontend — New Onboarding Page

- [ ] 8.1 Create `useOnboardingStore.ts` (Zustand): multi-step state, all field values, computed `canNext`, `isComplete`
- [ ] 8.2 Rewrite `OnboardingPage.tsx` with 7-step layout: progress bar + step title/description + content area + live summary sidebar
- [ ] 8.3 Step 1 — Job Role: searchable grid with categories (Professional / Student / Other), custom input
- [ ] 8.4 Step 2 — Industry: searchable grid, custom input
- [ ] 8.5 Step 3 — Conditional: Seniority selector (for professional) OR goal-oriented question (for student/career-change)
- [ ] 8.6 Step 4 — Communication: dual multi-select for "Who you communicate with" + "Communication channels"
- [ ] 8.7 Step 5 — Pain Points: multi-select chips grouped by category (vocabulary, grammar, pronunciation, confidence, specific situations)
- [ ] 8.8 Step 6 — Goals: preset goal chips + custom goal text input
- [ ] 8.9 Step 7 — Review: summary of all selections + AI persona preview + level selector + "Generate" button
- [ ] 8.10 Generation loading state: progress bar with "Generating topic X/25..." text, polling every 2 seconds
- [ ] 8.11 Ensure the new page fully replaces current `OnboardingPage.tsx` for Personalized flow

## Phase 9: Styling & Polish

- [ ] 9.1 Premium UI: empathetic step titles, gradient accents, smooth transitions between steps
- [ ] 9.2 Live summary panel: compact sidebar or floating card showing all selections, updating in real-time
- [ ] 9.3 Persona preview card: glassmorphism card with AI-generated summary, loading skeleton, fallback state
- [ ] 9.4 Search functionality: debounced search filter for role and industry grids
- [ ] 9.5 Mobile responsive: stack summary panel below content on small screens

## Phase 10: Verification

- [ ] 10.1 End-to-end: complete new onboarding → verify LearnerProfile in DB with correct payload
- [ ] 10.2 Level fix: select each level → verify in backend logs → verify in generated content
- [ ] 10.3 2-pass generation: verify 20-25 topics generated with 5 chunks × 3 phrases each
- [ ] 10.4 Progress tracking: verify progress bar updates during generation
- [ ] 10.5 Persona preview: verify AI preview displays, then test fallback by temporarily breaking LLM call
- [ ] 10.6 Image population: verify async image population runs after new path generation
- [ ] 10.7 Backward compatibility: verify Default catalog learning mode still works
