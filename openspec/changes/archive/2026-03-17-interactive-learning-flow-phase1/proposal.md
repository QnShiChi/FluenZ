## Why

Users currently only passively view learning content (Context Question, Root Sentence, Variable Chunks). There is no active practice mechanism — no quizzes, no pronunciation exercises, no way to measure mastery. Without an interactive loop, learners cannot transition from recognition to production, which is the core value proposition of the Pattern & Slotting method.

## What Changes

- **New quiz system**: Multiple-choice quiz for each component (Root + Chunks) — show Vietnamese, pick correct English from 3 options (1 correct + 2 LLM-generated distractors)
- **Speech recognition**: Browser-native Web Speech API captures user pronunciation as text
- **Pronunciation evaluation**: Backend Word-Level Levenshtein Distance algorithm scores accuracy (0-100) with word-level CORRECT/WRONG/MISSING feedback
- **Combination practice**: User speaks full sentences (Root + Chunk) with fill-in-the-blank variants
- **LLM prompt update**: Generate IPA phonetics and quiz distractors alongside learning content
- **Practice session state machine**: Zustand-based sequential flow managing Phase 1 (Isolation: Quiz → Speak) and Phase 2 (Combination: Full → Blank)
- **New database entities**: `PracticeSession` to track session metrics (scores, time, failed words)

## Capabilities

### New Capabilities
- `speech-evaluation`: Backend Word-Level Levenshtein algorithm for text comparison scoring, `POST /api/practice/evaluate-text` endpoint
- `practice-session`: Practice flow orchestration — `GET /api/practice/{situationId}/start` payload, `POST /api/practice/complete` session saving, state machine logic
- `practice-ui`: Frontend interactive screens — MultipleChoiceCard, AudioRecordButton, ScoreRing, useSpeechRecognition hook, usePracticeStore state machine

### Modified Capabilities
- None (existing specs unchanged; LLM prompt update is implementation detail)

## Impact

- **Backend**: New `SpeechEvaluationService`, `PracticeController`, `PracticeSession` entity, DTOs. LLM prompt adds `ipa` and `distractors` fields.
- **Frontend**: New practice page + Zustand store + reusable UI components. `SituationDetailPage` gains a working "Bắt đầu học" button.
- **Data model**: `SubPhrase` gains `ipa` column. New `PracticeSession` table. `Chunk` gains `rootIpa` column. LLM `variableChunks` become objects with `text`, `ipa`, `distractors`.
- **Dependencies**: No new external dependencies (Web Speech API is browser-native, Levenshtein is custom Java).
