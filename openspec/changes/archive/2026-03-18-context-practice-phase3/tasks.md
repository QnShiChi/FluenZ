## 1. Backend — Image Service (Unsplash)

- [x] 1.1 Add `UNSPLASH_ACCESS_KEY` to `.env` and `docker-compose.yml` env
- [x] 1.2 Create `ImageService.java` with `fetchImageUrl(String keyword)` using Unsplash API
- [x] 1.3 Handle rate limits and errors gracefully (return null on failure)

## 2. Backend — LLM Prompt + Data Model

- [x] 2.1 Add `imageKeyword` field to `LlmVariableChunk` DTO
- [x] 2.2 Update LLM prompt: add `imageKeyword` to variableChunk examples and rules
- [x] 2.3 Add `imageUrl` column to `SubPhrase` entity
- [x] 2.4 Update `OnboardingService`: call ImageService for each chunk, store imageUrl
- [x] 2.5 Update `SubPhraseResponse` DTO: add `imageUrl`
- [x] 2.6 Update `PracticeController` start payload: include `imageUrl`

## 3. Frontend — State Machine

- [x] 3.1 Add `CONTEXT_SPEAK` to `StepType` enum
- [x] 3.2 Add `contextQuestion`, `contextImage` fields to `PracticeStep` interface
- [x] 3.3 Add `imageUrl` to `VariableChunk` interface
- [x] 3.4 Generate Phase 3 steps in `generateSteps()`: 3× CONTEXT_SPEAK after Phase 2

## 4. Frontend — PracticePage UI

- [x] 4.1 Add `CONTEXT_SPEAK` rendering block: context question + image + mic + answer reveal
- [x] 4.2 Auto-play TTS for context question on step entry (reuse existing ttsPlaying logic)
- [x] 4.3 Show answer (full sentence) after evaluation regardless of score
- [x] 4.4 Hide answer when mic pressed to retry
- [x] 4.5 Apply ≥ 70 score gate (reuse existing logic)

## 5. Verification

- [x] 5.1 Rebuild Docker, generate new path with imageKeyword + imageUrl
- [x] 5.2 Test full practice flow in browser: Phase 1 → Phase 2 → Phase 3 (context question + image + answer reveal)
