## 1. Image Layout Fix

- [x] 1.1 Update CONTEXT_SPEAK image container: remove `max-h-48`, use `max-h-72` + `object-contain`
- [x] 1.2 Ensure image displays fully on desktop without cropping

## 2. Score Gate 3-Attempt Bypass

- [x] 2.1 Add `attemptCount` useState, initialize to 0
- [x] 2.2 Reset `attemptCount` to 0 on step transition (in existing useEffect)
- [x] 2.3 Increment `attemptCount` after each evaluation
- [x] 2.4 Update score gate logic: show "Tiếp tục" if `score ≥ 70` OR `attemptCount ≥ 3`
- [x] 2.5 Update retry message to show remaining attempts (e.g., "Lần 2/3 — Thử lại")
- [x] 2.6 Apply to ALL speaking steps (SPEAK, FULL_SENTENCE, FILL_BLANK, CONTEXT_SPEAK)

## 3. Verification

- [x] 3.1 User tests in browser
