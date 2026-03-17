## 1. Auto-play TTS on step entry

- [x] 1.1 Add `ttsPlaying` state to PracticePage
- [x] 1.2 Create `autoPlayTTS()` function that plays speakText() and sets ttsPlaying=true, with onend callback to set ttsPlaying=false
- [x] 1.3 Trigger autoPlayTTS on step transition (useEffect on currentStepIndex for SPEAK/FULL_SENTENCE/FILL_BLANK types)
- [x] 1.4 Disable AudioRecordButton while ttsPlaying is true

## 2. Score gate ≥ 70

- [x] 2.1 Add score threshold check after evaluationResult is set
- [x] 2.2 Hide "Tiếp tục →" button when score < 70
- [x] 2.3 Show retry message "Thử lại — bạn cần đạt ít nhất 70 điểm" when score < 70
- [x] 2.4 Allow mic press to retry (clears evaluationResult, re-records)

## 3. Fill-blank reveal/hide toggle

- [x] 3.1 Add `revealFullSentence` state to PracticePage
- [x] 3.2 Set revealFullSentence=true after evaluation in FILL_BLANK step
- [x] 3.3 Display fullSentence instead of blankDisplay when revealFullSentence=true
- [x] 3.4 Set revealFullSentence=false when mic pressed to retry
- [x] 3.5 Reset revealFullSentence on step transition

## 4. Verification

- [x] 4.1 Test auto-TTS plays on step entry and mic is disabled during playback
- [x] 4.2 Test score < 70 blocks "Tiếp tục" and shows retry message
- [x] 4.3 Test fill-blank reveals full sentence after speaking and re-hides on retry
