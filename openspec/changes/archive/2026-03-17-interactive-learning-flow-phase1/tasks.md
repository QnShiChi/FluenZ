## 1. Backend — Speech Evaluation Service

- [x] 1.1 Create `SpeechEvaluationService.java` with Word-Level Levenshtein algorithm
- [x] 1.2 Create `EvaluationResponseDTO.java` (overallScore, List<WordDetail>) and `WordDetail` (word, status enum)
- [x] 1.3 Create `POST /api/practice/evaluate-text` in `PracticeController.java`
- [x] 1.4 Test evaluation endpoint with curl (exact match, substitution, missing words)

## 2. Backend — LLM Prompt Update

- [x] 2.1 Update `LlmChunk` DTO: variableChunks becomes List<LlmVariableChunk> with text, ipa, distractors
- [x] 2.2 Add `rootIpa` field to `LlmChunk`
- [x] 2.3 Rewrite LLM prompt to include IPA and distractor generation
- [x] 2.4 Update `Chunk` entity: add `rootIpa` column
- [x] 2.5 Update `SubPhrase` entity: add `ipa` column and `distractors` (JSON text)
- [x] 2.6 Update `OnboardingService` entity building + mappers for new fields
- [x] 2.7 Update `ChunkResponse` / `SubPhraseResponse` DTOs

## 3. Backend — Practice Session

- [x] 3.1 Create `PracticeSession` entity (user FK, situation FK, totalTimeSeconds, overallScore, failedWords JSON, completedAt)
- [x] 3.2 Create `PracticeSessionRepository`
- [x] 3.3 Create `GET /api/practice/{situationId}/start` endpoint returning full practice payload
- [x] 3.4 Create `POST /api/practice/complete` endpoint saving session metrics

## 4. Frontend — State Machine & Types

- [x] 4.1 Define TypeScript interfaces: PracticePayload, PracticeStep, StepType enum
- [x] 4.2 Create `usePracticeStore` Zustand store with steps array and sequential progression
- [x] 4.3 Implement step generation logic from API payload (Phase 1: 4×Quiz+Speak, Phase 2: 3×Full+Blank)

## 5. Frontend — Speech Recognition Hook

- [x] 5.1 Create `useSpeechRecognition.ts` hook (Web Speech API, isListening, transcript, error)
- [x] 5.2 Handle browser compatibility (Chrome/Edge only)

## 6. Frontend — UI Components

- [x] 6.1 Create `MultipleChoiceCard` component (Vietnamese prompt, 3 English options, ding sound)
- [x] 6.2 Create `AudioRecordButton` component (mic toggle with waveform animation)
- [x] 6.3 Create `ScoreRing` component (circular progress 0-100)
- [x] 6.4 Create `WordFeedback` component (color-coded word list: green/red)

## 7. Frontend — Practice Page

- [x] 7.1 Create `PracticePage.tsx` with state machine rendering
- [x] 7.2 Add route `/practice/:situationId`
- [x] 7.3 Enable "Bắt đầu học" button on `SituationDetailPage`
- [x] 7.4 Integrate evaluate-text API call after speech capture

## 8. Verification

- [x] 8.1 Rebuild Docker, test evaluate-text endpoint
- [x] 8.2 Generate new path with IPA + distractors, verify payload
- [x] 8.3 Test full practice flow in browser (quiz → speak → score → next)
