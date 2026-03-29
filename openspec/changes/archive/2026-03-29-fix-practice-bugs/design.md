## Context

The system's practice flow currently contains several UX bugs related to presentation and Text-to-Speech (TTS) handling. These include showing placeholder strings in the multiple-choice quiz instead of actual data, the TTS literally reading `___` as "underscore" when phrases are blanked out, and failing to show the full reconstructed sentence to learners during speaking exercises. Additionally, system prompts inside the Roleplay phase ("Đến lượt bạn...") are inadvertently being processed by an English TTS engine, resulting in garbled audio.

## Goals / Non-Goals

**Goals:**
- Connect the multiple-choice quiz UI to the actual `distractors` data array provided by the backend interface.
- Pre-process strings before they hit the TTS engine to remove or replace literal `___` markers.
- Ensure the user sees the complete (assembled) sentence during FULL_SENTENCE and FILL_BLANK steps rather than a broken sentence.
- Prevent non-English system prompts in the Roleplay chat from triggering English TTS audio.

**Non-Goals:**
- Completely rewriting the `usePracticeStore` state machine.
- Redesigning the entire UI for the practice session components.
- Adding new properties or fields to the backend Database schema (we assume `distractors` is already passed down as a `string` via the API, delimited by `|`).

## Decisions

### Multiple Choice Options Generation
**Decision**: In `quiz.tsx` (or equivalent component), we will extract `target.subPhrase.distractors`, split it by `|`, and randomly select 2 items to combine with the correct translation. If fewer than 2 distractors exist in the DB for that phrase, we will provide safe hardcoded fallback strings to prevent crashes.

### TTS Underscore Handling
**Decision**: We will intercept the `playAudio` or `speak` function calls within the practice flow hooks/components. 
- For root sentences with blanks (`___`), we will use `string.replace('___', '...')` or `string.replace('___', targetPhrase)` depending on whether we want a pause or the full sentence spoken. 

### Reconstructing Full Sentences for UI
**Decision**: During `FULL_SENTENCE` and `FILL_BLANK` modes, the state/UI component will dynamically merge `rootSentence` and `subPhraseText`. Specifically: `rootSentence.replace('___', `<span class="text-highlight">${targetPhrase}</span>`)`. This avoids changing the source data while giving the user a complete sentence to read.

### Roleplay System Messages TTS
**Decision**: We will add a `skipTts: true` flag to system-generated message objects in the roleplay array, or explicitly filter out messages originating from `role: 'system'` before passing them to the TTS queue.

## Risks / Trade-offs

- **Risk: Malformed `distractors` string taking down the quiz** → *Mitigation*: The split logic will defensively handle empty or null strings by providing fallback distractors (e.g. `['alternative phrase 1', 'alternative phrase 2']`).
- **Risk: Sentence reconstruction fails if `___` is missing** → *Mitigation*: If `rootSentence` does not contain `___`, we will just append the target phrase or gracefully display both separately. 
