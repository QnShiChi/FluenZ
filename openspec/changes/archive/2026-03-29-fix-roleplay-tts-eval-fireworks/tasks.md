## 1. Roleplay TTS Recovery

- [ ] 1.1 Restore the opening English guidance cue before the first AI question
- [ ] 1.2 Restore the English handoff cue after the first AI acknowledgment and before the learner's question turn
- [ ] 1.3 Ensure these guidance cues are spoken without being inserted as visible transcript bubbles

## 2. Final-Turn Roleplay Contract Fix

- [ ] 2.1 Update backend roleplay prompt instructions so final turn behavior explicitly answers first, evaluates second
- [ ] 2.2 Rewrite backend final-turn example output without emoji
- [ ] 2.3 Verify frontend roleplay sequencing still submits the learner's final question as turn 2 input
- [ ] 2.4 Verify final AI output renders as `English answer` + `---` + `Vietnamese evaluation`

## 3. Emoji Removal

- [ ] 3.1 Remove emoji instructions from `RoleplayService`
- [ ] 3.2 Remove emoji from roleplay evaluation example text
- [ ] 3.3 Remove any remaining emoji-based copy in the affected roleplay/completion experience

## 4. Fireworks Library Integration

- [ ] 4.1 Add `@fireworks-js/react` to the frontend dependencies
- [ ] 4.2 Replace the custom completion celebration implementation with a library-backed fireworks component
- [ ] 4.3 Configure the fireworks to launch upward for a short one-time celebration
- [ ] 4.4 Ensure the effect stops after roughly 2-3 seconds and does not loop indefinitely
- [ ] 4.5 Preserve a reduced-motion-friendly fallback

## 5. Verification

- [ ] 5.1 Verify roleplay opening cue is spoken in English
- [ ] 5.2 Verify handoff cue is spoken in English before the learner asks their question
- [ ] 5.3 Verify final AI reply answers the learner's question before evaluation
- [ ] 5.4 Verify final evaluation block is emoji-free
- [ ] 5.5 Verify completion fireworks are visible and feel correct on screen entry
- [ ] 5.6 Run frontend build and confirm no regressions in the practice flow
