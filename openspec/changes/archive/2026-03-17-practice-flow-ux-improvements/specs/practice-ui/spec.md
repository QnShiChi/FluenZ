# practice-ui (Delta Spec)

## Changes

### Added Requirements

- **REQ-TTS-AUTOPLAY**: On entering any pronunciation step (SPEAK, FULL_SENTENCE, FILL_BLANK), automatically play TTS audio of the target phrase. Mic button must be disabled until TTS playback completes.
- **REQ-SCORE-GATE**: After speech evaluation, "Tiếp tục" button only appears if `overallScore ≥ 70`. Below 70, user must re-record. Show message "Thử lại — bạn cần đạt ít nhất 70 điểm".
- **REQ-BLANK-REVEAL**: In FILL_BLANK steps, reveal full sentence (replace blanks with actual text) after evaluation. Re-hide blanked portion when user presses mic to retry. Reset on step transition.
