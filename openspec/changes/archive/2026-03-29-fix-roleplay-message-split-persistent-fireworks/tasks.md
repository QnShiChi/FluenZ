## 1. Opening Guidance TTS

- [ ] 1.1 Ensure the roleplay screen always speaks the opening English guidance cue on first entry
- [ ] 1.2 Verify the opening cue plays before the first AI prompt is spoken
- [ ] 1.3 Keep the opening cue outside the visible transcript

## 2. Final Message Split

- [ ] 2.1 Parse the final AI response into `answer` and `evaluation` parts using the separator contract
- [ ] 2.2 Render the English answer as its own AI message block
- [ ] 2.3 Render the Vietnamese evaluation as a separate AI message block
- [ ] 2.4 Add a safe fallback when the separator is missing so roleplay does not break

## 3. Final-Turn TTS Behavior

- [ ] 3.1 Keep automatic TTS for the English final answer block
- [ ] 3.2 Do not require automatic TTS for the Vietnamese evaluation block
- [ ] 3.3 Ensure progression to the completion screen does not depend on evaluation TTS

## 4. Persistent Completion Fireworks

- [ ] 4.1 Update the `@fireworks-js/react` celebration lifecycle to stay active while the completion screen is mounted
- [ ] 4.2 Stop fireworks only when the learner exits via completion action or dashboard navigation
- [ ] 4.3 Tune intensity so persistent fireworks remain celebratory without overwhelming the screen

## 5. Verification

- [ ] 5.1 Verify the opening English cue is audible on roleplay entry
- [ ] 5.2 Verify the final AI answer and Vietnamese evaluation appear as two separate blocks
- [ ] 5.3 Verify the Vietnamese evaluation can remain silent without blocking the flow
- [ ] 5.4 Verify fireworks continue while the learner stays on the completion screen
- [ ] 5.5 Run frontend build and confirm the practice flow still compiles cleanly
