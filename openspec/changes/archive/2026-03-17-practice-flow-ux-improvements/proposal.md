## Why

The current practice flow has several UX issues that reduce learning effectiveness:
1. Users can immediately record their voice without first hearing the correct pronunciation
2. Users can skip past exercises they scored poorly on (even 0%), avoiding real practice
3. In fill-in-the-blank mode, the hidden text never reveals after speaking, making it impossible to self-check

## What Changes

- **Auto-play TTS on step entry**: When entering any SPEAK/FULL_SENTENCE/FILL_BLANK step, automatically play the reference audio via Web Speech Synthesis API. Disable the mic button until TTS finishes.
- **Minimum score gate (≥70)**: After speech evaluation, only show "Tiếp tục" button if overallScore ≥ 70. Below 70, user must retry by pressing mic again.
- **Fill-blank text reveal/hide toggle**: In FILL_BLANK steps, reveal the full sentence after evaluation. Re-hide when user presses mic to retry.

## Capabilities

### New Capabilities
_None — all changes are UX refinements within existing `practice-ui` capability._

### Modified Capabilities
- `practice-ui`: Adding auto-TTS, score gating, and fill-blank reveal logic to the practice step rendering

## Impact

- **Frontend only**: `PracticePage.tsx` — all 3 changes are in the practice step rendering logic
- No backend changes required
- No API changes
- No database changes
