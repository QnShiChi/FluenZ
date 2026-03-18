# practice-ui (Delta Spec)

## ADDED

- **REQ-ROLEPLAY-CHAT-STEP**: New `ROLEPLAY_CHAT` step type. Renders a Chat UI (Messenger-style bubbles) with mic-only input. One ROLEPLAY_CHAT step per practice session, appended after Phase 3 steps.

- **REQ-ROLEPLAY-5STEP-FLOW**: The roleplay follows a strict 5-step internal flow:
  1. System TTS intro (Vietnamese): "Hãy trả lời câu hỏi sau nha!"
  2. AI sends pre-generated first message (contextQuestion) + TTS English
  3. User speaks via mic → transcript appears as user bubble
  4. AI reply (real-time LLM) + TTS → then system TTS: "Đến lượt bạn hỏi mình đó"
  5. User speaks question → AI answers + evaluates (Vietnamese) + TTS → session ends

- **REQ-ROLEPLAY-NO-SCORE-GATE**: The roleplay phase SHALL NOT use score gating. The flow progresses based on conversation turns, not pronunciation scores.
