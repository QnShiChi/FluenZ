## Why

Phases 1-3 teach vocabulary, pronunciation, and contextual recall in isolation. But real-world English requires **spontaneous conversation**. Phase 4 bridges this gap by simulating a turn-based dialogue where the user applies everything they've learned in a realistic scenario.

## What Changes

- **New backend endpoint** `POST /api/practice/roleplay/chat` for real-time LLM conversation with dynamic prompting
- **New step type** `ROLEPLAY_CHAT` in the frontend practice flow
- **Chat UI** (Messenger-style bubbles + mic button) rendered inside PracticePage
- **Dynamic System Prompt** injected with role, situation, target chunks, and turn-based rules from DB

## Capabilities

### New Capabilities
- `roleplay-chat`: Backend endpoint + LLM dynamic prompting for turn-based roleplay

### Modified Capabilities
- `practice-ui`: New `ROLEPLAY_CHAT` step type + Chat UI rendering

## Impact

- **Backend**: New `RoleplayService.java`, new endpoint in `PracticeController.java`
- **Frontend**: New `ROLEPLAY_CHAT` step in store, Chat UI component in `PracticePage.tsx`
- **No DB schema changes** — uses existing `contextQuestion`, `situation`, `variableChunks`
