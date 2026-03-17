## ADDED Requirements

### Requirement: OpenRouter LLM service
The system SHALL provide a `LlmService` that:
- Calls OpenRouter's `/chat/completions` endpoint using RestTemplate
- Sends a structured prompt containing: profession, level, communication contexts, goals
- Requests JSON output matching the schema: topics[] → situations[] → chunks[] → subPhrases[]
- Parses the JSON response into domain objects
- Handles API errors with retry (up to 2 retries) and fallback error messages
- Reads config from env: `OPENROUTER_API_KEY`, `OPENROUTER_MODEL`, `OPENROUTER_BASE_URL`

#### Scenario: Successful generation
- **WHEN** the LLM returns valid JSON
- **THEN** the response is parsed into Topic/Situation/Chunk/SubPhrase objects

#### Scenario: LLM returns invalid JSON
- **WHEN** the LLM response cannot be parsed
- **THEN** the service retries up to 2 times, then throws an exception

---

### Requirement: LLM prompt design
The prompt SHALL instruct the LLM to generate:
- 3-5 topics per learning path
- 3-5 situations per topic (with Vietnamese context description + level)
- 3-5 chunks per situation (English question/phrase + Vietnamese translation)
- 2-4 sub-phrases per chunk (fill-in-the-blank phrases to memorize)

The prompt SHALL be in English and request output matching a specific JSON schema.

#### Scenario: Content quality
- **WHEN** a Software Engineer with BEGINNER level completes the survey
- **THEN** the generated situations are relevant to software engineering communication and appropriate for beginner level

---

### Requirement: OpenRouter configuration in application.yml
The system SHALL read OpenRouter config from `application.yml`:
```yaml
openrouter:
  api-key: ${OPENROUTER_API_KEY}
  model: ${OPENROUTER_MODEL:google/gemini-2.0-flash-001}
  base-url: ${OPENROUTER_BASE_URL:https://openrouter.ai/api/v1}
```

---

### Requirement: Docker Compose passes OpenRouter env vars
The `docker-compose.yml` backend service SHALL include OpenRouter env vars.
