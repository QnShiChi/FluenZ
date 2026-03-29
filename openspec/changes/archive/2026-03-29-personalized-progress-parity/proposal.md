## Why

Hiện tại progress tracking giữa hai learning mode đang không đồng nhất:

- `DEFAULT` path đã có logic đánh dấu chunk hoàn thành và cộng phút học.
- `PERSONALIZED` path lại đang có hành vi lệch, khiến user hoàn thành chunk nhưng không thấy tick completed và không được cộng thời gian học tương ứng.

Điều này làm trải nghiệm học bị thiếu nhất quán, khiến profile thống kê sai lệch, và làm cho personalized learning path kém tin cậy hơn default path.

Chúng ta cần đồng bộ logic để người học ở `PERSONALIZED` mode nhận cùng một behavior như `DEFAULT` mode cho:

- chunk completion
- subphrase learned state
- realtime learning minutes
- daily goal / streak impact

## What Changes

- Ensure `PERSONALIZED` chunk completion uses the same completion semantics as `DEFAULT`.
- Ensure `PERSONALIZED` chunk completion also marks nested subphrases as learned/completed where applicable.
- Ensure `PERSONALIZED` sessions contribute realtime learning minutes using the same rules as `DEFAULT`:
  - use `totalTimeSeconds`
  - minimum `1 minute` for non-zero durations under `60` seconds
  - cap at `10 minutes` per completed chunk
  - replaying an old chunk still adds time
- Ensure personalized progress responses return completed state so the UI can render tick/active status correctly.

## Capabilities

### Modified Capabilities
- `daily-learning-tracking`
- `chunk-completion-tracking`
- `progress-gamification-ui`

## Impact

- **Backend**: unify progress/time tracking behavior across `DEFAULT` and `PERSONALIZED` chunk-completion flows
- **Frontend**: personalized dashboards and situation detail screens can rely on the same completed-state behavior as default content
- **User experience**: learners receive consistent progress feedback regardless of learning mode
