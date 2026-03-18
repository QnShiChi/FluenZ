## Context

Phase 3 adds "Context Practice" to the existing practice flow (after Phase 1-2). Users hear the context question via TTS and must respond from memory without visual prompts. A stock image per chunk provides visual-associative memory cues. Existing stack: Spring Boot + React/Vite + Zustand + Unsplash API (new).

## Goals / Non-Goals

**Goals:**
- Unsplash image fetching service with abstraction layer (easy switch to AI-generated images later)
- LLM prompt extension: `imageKeyword` per variable chunk
- `CONTEXT_SPEAK` step type in Zustand store (appended after Phase 2)
- PracticePage rendering: context question + image → TTS → mic → evaluate → show answer

**Non-Goals:**
- AI image generation (DALL-E) — future upgrade path
- Custom image upload by users
- Phase 4-5 implementation

## Decisions

### 1. Image Source: Unsplash API (not DALL-E)
**Choice**: Free stock images from Unsplash via keyword search.
**Why**: Zero cost (50 req/hr free), fast response. Abstracted behind `ImageService` interface for future swap to AI generation.
**Trade-off**: Images may not perfectly match context (generic stock photos vs custom-generated).

### 2. Image fetching: At path creation time (not runtime)
**Choice**: Fetch image URLs during `OnboardingService.buildPath()` and store in `SubPhrase.imageUrl`.
**Why**: No latency during practice. Image URL is cached in DB. Falls back gracefully (no image shown) if Unsplash fails.

### 3. Step type: `CONTEXT_SPEAK` (separate from `SPEAK`)
**Choice**: New distinct step type to differentiate rendering logic.
**Why**: `CONTEXT_SPEAK` has unique behavior: shows context question (not target phrase), hides answer initially, reveals after evaluation. Different enough from `SPEAK` to warrant its own type.

### 4. Answer reveal: Always show after evaluation (regardless of score)
**Choice**: After evaluation, always display the correct full sentence on screen.
**Why**: Key pedagogical requirement — reinforces the correct answer through visual memory even when user scores low. User must still reach ≥ 70 to proceed (existing score gate).

## Files Changed

| File | Change |
|---|---|
| `backend/.../service/ImageService.java` | **[NEW]** Unsplash API client with `fetchImageUrl(keyword)` |
| `backend/.../service/LlmService.java` | Add `imageKeyword` to `LlmVariableChunk` + prompt |
| `backend/.../entity/SubPhrase.java` | Add `imageUrl` column |
| `backend/.../service/OnboardingService.java` | Call ImageService during path building, map imageUrl |
| `backend/.../controller/PracticeController.java` | Include `imageUrl` in start payload |
| `backend/.../dto/response/SubPhraseResponse.java` | Add `imageUrl` field |
| `.env` | Add `UNSPLASH_ACCESS_KEY` |
| `frontend/.../stores/usePracticeStore.ts` | Add `CONTEXT_SPEAK` type + Phase 3 step generation |
| `frontend/.../pages/PracticePage.tsx` | Render `CONTEXT_SPEAK` step (question + image + mic + answer reveal) |
