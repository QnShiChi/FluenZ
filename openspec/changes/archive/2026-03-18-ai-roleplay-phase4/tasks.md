## 1. Backend — RoleplayService

- [x] 1.1 Create `RoleplayService.java` with `chat(situationId, chatHistory, turnNumber)` method
- [x] 1.2 Build dynamic system prompt: inject AI role, user role, situation, target chunks
- [x] 1.3 Implement turn-based rules: turn 1 = acknowledge (max 2 sentences, no questions), turn 2 = answer + evaluate in Vietnamese
- [x] 1.4 Call OpenRouter LLM API and return response text

## 2. Backend — Endpoint

- [x] 2.1 Add `POST /api/practice/roleplay/chat` endpoint to PracticeController
- [x] 2.2 Accept `{ situationId, chatHistory: [{role, text}], turnNumber }` request body
- [x] 2.3 Load situation + chunks + variableChunks from DB for context injection
- [x] 2.4 Return `{ response: "AI text" }`

## 3. Frontend — Store

- [x] 3.1 Add `ROLEPLAY_CHAT` to StepType enum
- [x] 3.2 Add `chatMessages: {role: 'system'|'ai'|'user', text: string}[]` to PracticeStep
- [x] 3.3 Generate 1 ROLEPLAY_CHAT step after Phase 3 steps in `generateSteps()`
- [x] 3.4 Pre-populate with contextQuestion as AI first message

## 4. Frontend — Chat UI in PracticePage

- [x] 4.1 Render Chat UI when step type is ROLEPLAY_CHAT: chat bubbles + mic button
- [x] 4.2 Internal state machine: roleplayPhase (1→2→3→4→5)
- [x] 4.3 Phase 1: Play system TTS "Hãy trả lời câu hỏi sau nha!" → auto-advance
- [x] 4.4 Phase 2: Show AI bubble + TTS contextQuestion → enable mic
- [x] 4.5 Phase 3: User speaks → add user bubble → call /roleplay/chat (turn 1) → advance
- [x] 4.6 Phase 4: Show AI reply bubble + TTS → when done → system TTS "Đến lượt bạn hỏi mình đó" → enable mic
- [x] 4.7 Phase 5: User speaks → call /roleplay/chat (turn 2) → show AI final bubble + TTS → session complete

## 5. Verification

- [x] 5.1 Rebuild Docker, test full flow Phase 1 → 2 → 3 → 4
- [x] 5.2 User tests in browser
