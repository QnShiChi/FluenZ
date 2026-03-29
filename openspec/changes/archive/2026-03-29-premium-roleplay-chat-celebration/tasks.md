## 1. Roleplay Chat Redesign

- [ ] 1.1 Redesign `RoleplayChatUI` into a premium coach conversation layout with a transcript area, cleaner header, and bottom composer/action dock
- [ ] 1.2 Remove emoji and overt exercise-like system messaging from roleplay and completion surfaces
- [ ] 1.3 Add distinct AI and learner bubble styles with improved spacing, alignment, and readability
- [ ] 1.4 Replace transcript-breaking system bubbles with subtle guidance near the composer/status area

## 2. Voice-First Interaction

- [ ] 2.1 Add an explicit `Speak` / `Type` switch for roleplay input
- [ ] 2.2 Keep `Speak` as the default and visually primary mode
- [ ] 2.3 Reveal the text input field only when the learner switches to `Type`
- [ ] 2.4 Route typed learner messages through the same chat turn flow as spoken learner messages

## 3. AI Response Presentation

- [ ] 3.1 Add a frontend-only streaming text reveal for AI responses after full backend response receipt
- [ ] 3.2 Add a restrained typing/thinking indicator before streamed AI text begins
- [ ] 3.3 Keep learner messages rendering instantly without streaming
- [ ] 3.4 Sequence auto TTS cleanly with the streamed AI message lifecycle
- [ ] 3.5 Respect reduced-motion preferences by shortening or simplifying the streaming effect

## 4. Completion Celebration

- [ ] 4.1 Add a one-time fireworks celebration overlay that launches upward when the completion screen is entered
- [ ] 4.2 Limit the celebration to roughly 2-3 seconds and prevent looping
- [ ] 4.3 Ensure celebration motion does not block score readability or the return action
- [ ] 4.4 Respect reduced-motion preferences with a toned-down completion effect

## 5. Visual System and Refactor

- [ ] 5.1 Apply premium tutor/coach styling to the roleplay and completion experience
- [ ] 5.2 Extract conversation and celebration responsibilities into smaller reusable frontend components where helpful
- [ ] 5.3 Keep the implementation free of backend API contract changes for this phase

## 6. Verification

- [ ] 6.1 Verify voice-first roleplay flow works end-to-end in `Speak` mode
- [ ] 6.2 Verify typed fallback flow works end-to-end in `Type` mode
- [ ] 6.3 Verify AI streaming presentation and TTS sequencing feel coherent in the browser
- [ ] 6.4 Verify completion fireworks trigger once on screen entry and do not repeat unnecessarily
- [ ] 6.5 Run frontend build and confirm no regressions in practice flow routing
