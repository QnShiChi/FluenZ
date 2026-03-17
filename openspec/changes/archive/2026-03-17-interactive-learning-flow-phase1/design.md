## Context

FluenZ uses a Pattern & Slotting learning model: each Situation contains Chunks with a Context Question, Root Sentence, and Variable Chunks. Currently the `SituationDetailPage` only shows static content. This design covers adding Phase 1 (Component Isolation: Quiz + Speak) and Phase 2 (Combination & Reflex: Full + Blank sentence practice) to create an active learning loop.

Existing stack: Spring Boot backend (JPA/Postgres), React + Vite frontend, OpenRouter LLM, Docker Compose.

## Goals / Non-Goals

**Goals:**
- Word-Level Levenshtein evaluation service with word-level CORRECT/WRONG/MISSING feedback
- Practice session orchestration API (start/complete) with payload for the frontend state machine
- Zustand-based state machine managing sequential Phase 1-2 transitions (~14 steps per session)
- Web Speech API integration for capturing pronunciation
- LLM prompt update to generate IPA phonetics and quiz distractors
- Reusable UI components: MultipleChoiceCard, AudioRecordButton, ScoreRing, WordFeedback

**Non-Goals:**
- Phase 3-5 (Context practice, AI Roleplay, Clinic/Gamification) — separate future change
- Real pronunciation assessment (pitch/stress analysis) — Levenshtein is word-match only
- Offline speech recognition — requires internet for Web Speech API
- Mobile-native audio handling — browser-only

## Decisions

### 1. Speech Evaluation: Word-Level Levenshtein (not Azure/Google)
**Choice**: Custom Java algorithm comparing Web Speech API transcript against expected text.
**Why**: Zero cost, no API keys, sufficient for MVP (word match accuracy). Azure Speech Assessment costs ~$1/1000 calls and requires SDK setup.
**Trade-off**: No phoneme-level feedback (can't detect subtle mispronunciations within a correctly-recognized word).

### 2. State Machine: Zustand store (not XState)
**Choice**: Flat `usePracticeStore` with `currentPhase`, `currentStepIndex`, and `steps[]` array.
**Why**: XState adds 30KB+ bundle and learning curve. Zustand is already in the project. The flow is strictly sequential (no branching/parallel states), so a simple array index with step types is sufficient.

### 3. IPA & Distractors: LLM-generated at path creation time
**Choice**: Extend LLM prompt to include `ipa` and `distractors[]` in each chunk/subPhrase.
**Why**: Runtime IPA generation would require an extra API call per practice session. Pre-generating at path creation keeps the practice flow fast (no LLM latency).
**Trade-off**: Larger LLM response payload. IPA accuracy depends on LLM quality.

### 4. Audio capture: MediaRecorder API → Web Speech API
**Choice**: Use `window.SpeechRecognition` (or `webkitSpeechRecognition`) for real-time STT.
**Why**: Browser-native, no dependencies. Returns transcript text that can be sent to backend for Levenshtein comparison.
**Limitation**: Not available in Firefox (Chrome/Edge only). Accuracy varies by accent.

### 5. Practice Session entity: Lightweight metrics only
**Choice**: `PracticeSession` table stores: userId, situationId, totalTime, overallScore, failedWords (JSON), completedAt.
**Why**: Enables Post-Session Clinic (Phase 5 future) and progress tracking. JSON column for failed words avoids a join table.

## Risks / Trade-offs

- **Web Speech API browser support** → Only Chrome/Edge. Mitigation: Show "Use Chrome for best experience" warning on unsupported browsers.
- **LLM IPA accuracy** → LLM may produce incorrect IPA. Mitigation: Acceptable for MVP; can be replaced with dictionary lookup later.
- **Levenshtein false positives** → Web Speech API may transcribe differently than expected (e.g., "due" → "do"). Mitigation: Normalize text (lowercase, remove punctuation) before comparison.
- **Session data loss on page refresh** → Zustand store is in-memory. Mitigation: Acceptable for MVP; user restarts session.
