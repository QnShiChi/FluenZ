## ADDED Requirements

### Requirement: Text comparison evaluation endpoint
The system SHALL provide a `POST /api/practice/evaluate-text` endpoint that accepts `expectedText` and `actualText` strings and returns an evaluation result.

#### Scenario: Exact match
- **WHEN** actualText equals expectedText (case-insensitive, punctuation-ignored)
- **THEN** system returns overallScore of 100 and all words marked CORRECT

#### Scenario: Partial match with substitution
- **WHEN** actualText is "due to now requirements" and expectedText is "due to new requirements"
- **THEN** system returns overallScore < 100, "new" marked as WRONG, other words marked CORRECT

#### Scenario: Missing words
- **WHEN** actualText has fewer words than expectedText
- **THEN** missing words are marked as MISSING in the wordDetails list

### Requirement: Word-Level Levenshtein algorithm
The system SHALL use a Word-Level Levenshtein Distance algorithm to compare tokenized word arrays. Words SHALL be compared case-insensitively with punctuation stripped.

#### Scenario: Score calculation
- **WHEN** evaluation is performed
- **THEN** overallScore = max(0, 100 - (errorCount / totalExpectedWords * 100)), rounded to integer

### Requirement: Evaluation response format
The response SHALL contain `overallScore` (int 0-100) and `wordDetails` (List) where each WordDetail has `word` (String) and `status` (Enum: CORRECT, WRONG, MISSING).

#### Scenario: Response structure
- **WHEN** evaluation completes
- **THEN** response JSON has shape: `{ "overallScore": 75, "wordDetails": [{"word": "due", "status": "CORRECT"}, ...] }`
