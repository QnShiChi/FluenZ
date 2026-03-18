## Why

Two UX issues reduce learning effectiveness:
1. **Context image cropped on desktop**: The `max-h-48` constraint cuts off Unsplash images, making it hard for users to associate the visual context with the phrase. Full visibility is essential for visual-associative memory.
2. **Score gate too strict**: Users who struggle with a phrase get stuck indefinitely. After 3 failed attempts (< 70), they should be allowed to continue — retaining learning momentum is more important than perfect scores.

## What Changes

- **Image layout**: Remove restrictive height cap on context images. Use responsive sizing that shows full image on desktop.
- **3-attempt bypass**: Add `attemptCount` state. After 3 evaluations below 70, show "Tiếp tục" button anyway. Applies to ALL speaking step types (SPEAK, FULL_SENTENCE, FILL_BLANK, CONTEXT_SPEAK).

## Capabilities

### Modified Capabilities
- `practice-ui`: Image layout fix + score gate retry logic

## Impact

- **Frontend only**: `PracticePage.tsx` — image CSS + attemptCount state logic. No backend changes.
