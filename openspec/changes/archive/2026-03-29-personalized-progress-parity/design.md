## Overview

This change establishes behavior parity between `DEFAULT` and `PERSONALIZED` learning modes for progress tracking.

The key rule is:

- learning mode should change content source
- learning mode should not change the user's progress semantics

If a user completes a chunk in either mode, the system should:

1. mark the chunk as completed
2. mark relevant subphrases as learned
3. add realtime learning minutes based on the same duration rules
4. update daily goal and streak consistently

## Progress Parity

For `PERSONALIZED` chunks, the system should follow the same business rules already expected from `DEFAULT` chunks:

- chunk completion is persistent
- replay does not remove completed state
- replay still contributes realtime minutes
- nested phrase progress reflects completion

## Time Calculation

Realtime minute calculation should remain identical across both modes:

- source: `totalTimeSeconds`
- if `0` or invalid: add `0`
- if `0 < seconds < 60`: add `1 minute`
- otherwise round up to the next minute
- cap at `10 minutes` per chunk completion

## Response Parity

Progress-bearing responses used by the UI should expose completed state consistently for personalized content as well as default content.

That means:

- personalized situation/chunk responses should include completion state
- UI should not need special-case logic to infer personalized completion differently from default completion

## Risks

- Fixing only backend write logic without fixing personalized response mapping would still leave the UI unable to display completion correctly.
- Fixing only UI mapping without fixing backend minute accumulation would still leave profile time incorrect.

## Success Criteria

- Completing a chunk in `PERSONALIZED` mode updates both completion state and learning minutes.
- Replaying a personalized chunk still adds realtime learning minutes.
- Personalized chunks render completed state in the UI just like default chunks.
- Daily goal and streak react the same way in both modes.
