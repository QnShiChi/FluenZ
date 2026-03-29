## 1. Multiple Choice Quiz Fixes
- [x] 1.1 Extract actual distractors and correct answer in `MultipleChoiceCard` (or parent component).
- [x] 1.2 Implement shuffling logic to combine distractors and the correct answer.
- [x] 1.3 Add defensive fallback (e.g. `['alternative 1', 'alternative 2']`) if distractors are missing or malformed to prevent crashes.

## 2. TTS Underscore Handling
- [x] 2.1 Identify the central TTS function/hook (e.g., `speak` in `useTTS` or where `ttsApi` is called).
- [x] 2.2 Add pre-processing logic to strip out `___` when sending text to the TTS engine.
- [x] 2.3 For FILL_BLANK/FULL_SENTENCE steps, substitute `___` with the target `subPhrase` or `chunk` text so the full sentence is spoken.

## 3. UI Sentence Reconstruction
- [x] 3.1 Update `FULL_SENTENCE` and `FILL_BLANK` UI components to dynamically replace `___` in the root sentence with the learned target phrase for display.
- [x] 3.2 Ensure the replaced part is visually distinct (e.g. highlighted) if needed, or simply present the complete sentence to the user.

## 4. Roleplay TTS Filtering
- [x] 4.1 Locate the Roleplay message rendering and TTS playback logic.
- [x] 4.2 Prevent system-generated messages (like "Hãy trả lời câu hỏi sau nhé" or "Đến lượt bạn hỏi mình đó") from being sent to the English TTS engine.
