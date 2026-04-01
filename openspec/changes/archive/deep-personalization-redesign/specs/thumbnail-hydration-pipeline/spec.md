## ADDED Requirements

### Requirement: Guaranteed thumbnail coverage
Every generated personalized situation MUST eventually have a thumbnail.

#### Scenario: Situation missing exact-match image
- **WHEN** the system cannot obtain a suitable thumbnail from the initial exact situation query
- **THEN** it MUST continue through fallback strategies until a thumbnail is assigned
- **AND** the final state MUST NOT leave the situation with a null or empty thumbnail

### Requirement: External-first fallback pipeline
The thumbnail pipeline SHALL prefer external image sources first and only use the curated internal pool as the final guaranteed fallback.

#### Scenario: External-first retrieval
- **WHEN** a situation needs a thumbnail
- **THEN** the system MUST try an exact situation query first
- **AND THEN** a broader semantic query if needed
- **AND THEN** a generic professional/work/communication query if needed
- **AND** only after those attempts fail or return unusable duplicates SHOULD the system use the curated internal fallback pool

### Requirement: Per-path thumbnail dedupe
The system MUST prevent obvious duplicate thumbnails within the same personalized learning path.

#### Scenario: Candidate image already used in this path
- **WHEN** a candidate thumbnail matches a previously used image in the same personalized path by source URL, fingerprint, or equivalent duplicate marker
- **THEN** the system MUST reject that candidate for the current situation
- **AND** it MUST retry with a different query, candidate, source, or fallback tier

### Requirement: Thumbnail lineage metadata
The system MUST retain enough metadata to debug and retry thumbnail generation.

#### Scenario: Thumbnail generation metadata recorded
- **WHEN** the system attempts to fetch or assign a thumbnail
- **THEN** it MUST store or make available metadata including original query, fallback query, selected source, selected image URL, fingerprint/duplicate marker, retry count, and generation status

### Requirement: Async hydration after usable text publish
The system SHALL allow personalized text content to become usable before all thumbnails are fully hydrated.

#### Scenario: Text path published before thumbnails finish
- **WHEN** personalized content generation completes before all thumbnails are fetched
- **THEN** the system MAY publish the usable text path first
- **AND** thumbnail hydration MUST continue asynchronously in the background
- **AND** the final state MUST still satisfy full thumbnail coverage and per-path dedupe requirements
