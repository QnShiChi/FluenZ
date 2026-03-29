## Why

The current learning practice flow has 4 distinct UX/functional bugs that disrupt the user experience:
1. The quiz phase shows placeholder "alternative phrase 1/2" instead of the actual distractors from the sub-phrase data.
2. The Text-to-Speech (TTS) engine reads the literal `___` (underscore) characters in the root sentence, which sounds unnatural ("underscore, underscore...").
3. In the speaking phases ("Tập nói câu hoàn chỉnh" and "Nhìn ảnh nói câu"), the UI only displays the incomplete root sentence containing `___`, rather than the full, assembled sentence, leaving learners confused about what to say.
4. In the AI Roleplay phase, the AI TTS attempts to read Vietnamese system messages ("Hãy trả lời câu hỏi sau nhé" and "Đến lượt bạn hỏi mình đó") but fails (likely using an English voice for Vietnamese text or reading irrelevant metadata), resulting in unintelligible audio.

These issues directly impact the core learning loop, reducing the quality of the practice sessions and making the product feel unpolished. Fixing them is critical for a smooth user experience.

## What Changes

- **Quiz Distractors**: Update the "Câu gốc — Trắc nghiệm" phase to parse and display real distractors from the sub-phrase data instead of hardcoded placeholders.
- **TTS Underscores**: Pre-process text sent to the TTS engine to replace `___` with a brief pause or the actual missing word, or strip it entirely if the context demands just the root sentence without the blank.
- **Speaking Phase UI**: Dynamically reassemble the complete sentence by replacing `___` in the root sentence with the learned sub-phrase, and display this complete sentence in the speaking practice and roleplay phases.
- **AI Roleplay TTS**: Disable TTS for system prompt messages ("Hãy trả lời...", "Đến lượt bạn...") OR ensure they are passed to a Vietnamese-compatible TTS voice. Since these are UI instructions, disabling TTS for them and only playing TTS for the actual AI conversational response is the preferred approach.

## Capabilities

### New Capabilities
None.

### Modified Capabilities
- `learning-practice-flow`: Modifying the presentation and TTS handling of practice exercises to ensure correct data binding and audio output.

## Impact

- Frontend practice flow components (Quiz, Speaking, AI Roleplay).
- TTS audio generation logic (pre-processing text before sending to the TTS service/API).
- Data mapping in the practice flow (extracting `distractors` from the `SubPhrase` object).
