# context-images (New Capability)

## Requirements

### Requirement: Image Service
The backend SHALL provide an `ImageService` that fetches stock image URLs from Unsplash API based on a keyword. The service SHALL be abstracted behind an interface for future swap to AI-generated images.

#### Scenario: Successful image fetch
- **WHEN** `fetchImageUrl("software meeting")` is called
- **THEN** return a valid Unsplash image URL (small size, landscape orientation)

#### Scenario: API failure or rate limit
- **WHEN** Unsplash API returns error or rate limit exceeded
- **THEN** return null gracefully (no image shown in UI, non-blocking)

### Requirement: Image keyword generation via LLM
The LLM prompt SHALL generate an `imageKeyword` field for each `variableChunk` — a short English phrase (2-4 words) suitable for stock image search.

#### Scenario: LLM generates image keywords
- **WHEN** learning path is created
- **THEN** each variableChunk includes `imageKeyword` (e.g., "deadline pressure office", "user feedback laptop")

### Requirement: Image URL storage
The `SubPhrase` entity SHALL store the fetched image URL in an `imageUrl` column. The URL SHALL be fetched at path creation time and persisted in the database.
