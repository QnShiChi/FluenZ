## ADDED Requirements

### Requirement: Strict subphrase quality rules
Generated subphrases MUST be usable phrases rather than isolated single-word tokens.

#### Scenario: Validate generated subphrases
- **WHEN** the system generates a chunk for a personalized path
- **THEN** it MUST ensure the chunk contains exactly 3 subphrases
- **AND** each subphrase SHOULD default to a usable phrase of 2 to 6 words
- **AND** one-word subphrases MUST be rejected by default unless explicitly justified as a special-case exception

### Requirement: Subphrases support root sentence construction
Subphrases within a chunk MUST meaningfully support building the chunk's root sentence.

#### Scenario: Phrase usefulness validation
- **WHEN** a chunk is generated
- **THEN** the system MUST validate that the 3 subphrases are not arbitrary fragments
- **AND** the set of subphrases MUST help the learner assemble or understand the root sentence progressively

### Requirement: Targeted regeneration on validation failure
The system SHALL retry only the invalid generation units instead of regenerating the full learning path.

#### Scenario: Reject weak subphrases
- **WHEN** subphrase validation fails for a generated chunk
- **THEN** the system MUST regenerate only that chunk or its subphrases
- **AND** it MUST preserve already valid topics, situations, chunks, and thumbnails

### Requirement: Strict situation content contract
Each generated personalized situation SHALL contain exactly 5 chunks, and each chunk SHALL contain exactly 3 validated subphrases.

#### Scenario: Situation contract verification
- **WHEN** a generated situation is validated before publication
- **THEN** the system MUST reject or repair any situation that does not contain exactly 5 chunks or any chunk that does not contain exactly 3 validated subphrases
