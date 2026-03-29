## Context

LLM prompt and situation detail page need fixes to enforce data consistency and improve UX.

## Goals / Non-Goals

**Goals:**
- LLM always generates exactly 3 variableChunks per chunk
- Trunk (rootSentence) visually distinct from subphrases on situation detail page

**Non-Goals:**
- Changing practice flow logic (Phases 1-4 unchanged)
- Changing the number of chunks per situation

## Decisions

### 1. LLM enforcement: explicit rule + example
**Choice**: Add `"CRITICAL: You MUST generate EXACTLY 3 variableChunks per chunk."` to system prompt with examples showing 3 chunks.
**Why**: LLM follows explicit constraints better when reinforced with examples.

### 2. UI: separate sections with labels
**Choice**: Split "BẠN CẦN NHỚ CỤM SAU" into two sections:
- "CÂU GỐC" — trunk card with primary border, larger font, puzzle icon
- "CỤM TỪ CẦN NHỚ (3)" — numbered subphrase cards with lighter style
**Why**: Clear visual hierarchy helps users understand the trunk+chunk learning model.

## Files Changed

| File | Change |
|---|---|
| `backend/.../service/LlmService.java` | Add "exactly 3 variableChunks" rule to prompt |
| `frontend/.../pages/SituationDetailPage.tsx` | Restructure trunk/subphrase sections |
