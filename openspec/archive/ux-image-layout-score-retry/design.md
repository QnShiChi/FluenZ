## Context

Frontend-only UX fixes to PracticePage.tsx. No backend changes needed.

## Goals / Non-Goals

**Goals:**
- Full context image display on desktop (remove max-h-48 crop)
- 3-attempt bypass for score gate across all speaking steps

**Non-Goals:**
- Changing the 70-point threshold itself
- Backend scoring changes

## Decisions

### 1. Image sizing: max-h-72 with object-contain
**Choice**: Increase max height and use `object-contain` instead of `object-cover` so the full image is always visible.
**Why**: `object-cover` crops the image to fill the container; `object-contain` shows the complete image within bounds.

### 2. Attempt counter: useState per step
**Choice**: `attemptCount` state, reset to 0 on step transition, increment after each evaluation.
**Why**: Simple, no store changes needed. The counter is UI-local state, not persisted.

## Files Changed

| File | Change |
|---|---|
| `frontend/.../pages/PracticePage.tsx` | Image CSS fix + attemptCount logic |
