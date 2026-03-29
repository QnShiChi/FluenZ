## 1. Backend Completion Parity

- [x] 1.1 Verify `POST /api/progress/chunk-complete/{chunkId}` routes correctly for both `DEFAULT` and `PERSONALIZED` learning modes
- [x] 1.2 Ensure personalized chunk completion persists `UserChunkProgress` with `is_completed = true`
- [x] 1.3 Ensure personalized chunk completion also marks nested `UserSubPhraseProgress` records as learned

## 2. Backend Time Tracking Parity

- [x] 2.1 Ensure personalized chunk completion uses realtime minute accumulation from `totalTimeSeconds`
- [x] 2.2 Apply the same rounding and `10 minute` cap rules used by default content
- [x] 2.3 Ensure replaying a previously completed personalized chunk still increases learning minutes

## 3. Response And UI Parity

- [x] 3.1 Ensure personalized learning path responses expose chunk completed state for dashboard and situation detail UI
- [x] 3.2 Verify personalized chunk completion appears as active/ticked in the UI
- [x] 3.3 Ensure personalized mode toast/profile updates reflect the gained realtime minutes

## 4. Verification

- [x] 4.1 Verify new personalized chunk completion marks completed state immediately
- [x] 4.2 Verify new personalized chunk completion increases `todayMinutes` and `totalLearningMinutes`
- [x] 4.3 Verify replaying a personalized chunk still adds time without removing completed state
- [x] 4.4 Verify default mode behavior remains unchanged
