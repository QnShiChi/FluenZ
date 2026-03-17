## Overview

Three frontend-only UX improvements to `PracticePage.tsx` that improve learning effectiveness by enforcing listen-first flow, score gating, and visual feedback.

## Technical Decisions

### 1. Auto-play TTS on step entry
- Use `window.speechSynthesis` (already available via `speakText()` helper)
- Add `ttsPlaying` state to track when TTS is active
- On `onend` event of `SpeechSynthesisUtterance`, set `ttsPlaying = false`
- Mic button disabled while `ttsPlaying === true`
- Auto-trigger on every SPEAK/FULL_SENTENCE/FILL_BLANK step transition

### 2. Score gate ≥ 70
- After evaluation, check `evaluationResult.overallScore`
- If `< 70`: hide "Tiếp tục" button, show retry prompt "Thử lại — bạn cần đạt ít nhất 70 điểm"
- If `≥ 70`: show "Tiếp tục →" button normally
- User retries by pressing mic again → clears previous result, re-records

### 3. Fill-blank reveal/hide
- Add `revealFullSentence` boolean state
- After evaluation in FILL_BLANK step: set `revealFullSentence = true` → display `currentStep.fullSentence` instead of `blankDisplay`
- When mic pressed again to retry: set `revealFullSentence = false` → show `blankDisplay` again
- Reset on step transition

## Files Changed

| File | Change |
|---|---|
| `frontend/src/pages/PracticePage.tsx` | All 3 features: TTS auto-play, score gate, reveal toggle |

No backend changes. No new dependencies.
