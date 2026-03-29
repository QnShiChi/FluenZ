# practice-ui (Delta Spec)

## MODIFIED

- **REQ-CONTEXT-IMAGE-LAYOUT**: Context images in CONTEXT_SPEAK steps SHALL display fully without cropping. Use `object-contain` with adequate max height for desktop readability.
- **REQ-SCORE-GATE-RETRY**: The score gate (≥ 70 to continue) SHALL allow bypass after 3 failed attempts. After 3 evaluations scoring below 70, the "Tiếp tục" button SHALL appear regardless of score. Attempt counter resets on step transition. Applies to ALL speaking step types: SPEAK, FULL_SENTENCE, FILL_BLANK, CONTEXT_SPEAK.
