## 1. Roleplay Opening Cue

- [ ] 1.1 Audit the current opening roleplay cue path and identify why it is still not reliably audible
- [ ] 1.2 Ensure the opening English cue is played before the first AI question is spoken
- [ ] 1.3 Prevent learner input from unlocking until the opening cue and first AI prompt are finished

## 2. Final Turn Refinement

- [ ] 2.1 Tighten backend roleplay prompt behavior so the final turn answers first and evaluates second
- [ ] 2.2 Verify the final AI response visibly preserves the `English answer` + `---` + `Vietnamese evaluation` structure
- [ ] 2.3 Ensure the final turn no longer feels like it jumps straight into evaluation

## 3. Manual Continue Gate

- [ ] 3.1 Add a terminal roleplay UI state after the final AI turn completes
- [ ] 3.2 Disable mic and typed input in the completed roleplay state
- [ ] 3.3 Add a visible `Tiếp tục` button beneath or near the final transcript state
- [ ] 3.4 Transition to the completion screen only when the learner clicks `Tiếp tục`

## 4. Fireworks Visual Tuning

- [ ] 4.1 Retune the `@fireworks-js/react` palette toward warmer, higher-contrast celebratory colors
- [ ] 4.2 Increase the effect visibility enough that the fireworks are immediately noticeable
- [ ] 4.3 Preserve readability of score summaries and completion actions while the stronger fireworks are active

## 5. Verification

- [ ] 5.1 Verify the opening cue is audible at roleplay start
- [ ] 5.2 Verify the final AI turn answers the learner's question before evaluating
- [ ] 5.3 Verify roleplay no longer auto-skips into completion
- [ ] 5.4 Verify `Tiếp tục` is required to enter the completion screen
- [ ] 5.5 Verify the updated fireworks are clearly visible and visually compatible with the product style
