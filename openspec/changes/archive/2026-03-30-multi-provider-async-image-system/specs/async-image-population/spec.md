## ADDED Requirements

### Requirement: Non-blocking path creation
The system MUST return the learning path response to the user immediately after saving entities, without waiting for images to be fetched.

#### Scenario: Creating a new personalized learning path
- **WHEN** a user completes onboarding and the LLM generates a learning path
- **THEN** the system MUST save all entities (path, topics, situations, chunks, subphrases) with `imageUrl = null`
- **AND** the system MUST return the path response to the user immediately
- **AND** the system MUST trigger an asynchronous background job to populate images

#### Scenario: User views path before images are ready
- **WHEN** a user fetches their active learning path while image population is still in progress
- **THEN** the system MUST return the path with whatever image URLs are currently available
- **AND** any SubPhrase or Situation without an image MUST return `imageUrl = null`

### Requirement: Background image population
The system MUST populate images for all SubPhrases and Situations in a learning path via an asynchronous background process after path creation.

#### Scenario: Async job populates SubPhrase images
- **WHEN** the background image population job runs for a new learning path
- **THEN** the system MUST fetch images for every SubPhrase that has a non-blank `imageKeyword` (from the LLM response)
- **AND** the system MUST use the multi-provider image chain (with cache) for each fetch
- **AND** the system MUST update each SubPhrase's `imageUrl` in the database as each image is resolved
- **AND** image fetches MUST execute in parallel with a bounded thread pool (max 10 concurrent)

#### Scenario: Async job populates Situation thumbnails
- **WHEN** the background image population job runs for a new learning path
- **THEN** the system MUST fetch a thumbnail image for every Situation that has a non-blank `imageKeyword`
- **AND** the system MUST update each Situation's `thumbnailUrl` in the database

#### Scenario: Async job failure does not affect path
- **WHEN** the background image population job encounters an error (network, API, etc.)
- **THEN** the system MUST log the error
- **AND** the system MUST NOT modify or delete the learning path
- **AND** SubPhrases and Situations that failed image fetch MUST retain `imageUrl = null`

### Requirement: LLM situation image keyword generation
The LLM MUST generate an `imageKeyword` for each Situation in the learning path, in addition to the existing SubPhrase `imageKeyword`.

#### Scenario: LLM generates situation-level image keywords
- **WHEN** the LLM generates a personalized learning path
- **THEN** each Situation in the response MUST include an `imageKeyword` field
- **AND** the keyword MUST be a 2–4 word English phrase suitable for stock photo search
- **AND** the keyword MUST be contextually relevant to the situation's communication scenario
