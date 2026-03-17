## Why

Auth and data models are done, but users have no way to get personalized learning content. This module lets users complete an onboarding survey, sends their profile to an LLM (via OpenRouter), and auto-generates a structured learning path with topics, situations, and chunks. This is the core differentiator of FluenZ — every user gets a unique path based on their profession and goals.

## What Changes

- Add `Topic` entity between LearningPath and Situation (grouping layer)
- Add `SubPhrase` entity under Chunk (fill-in-the-blank phrases to memorize)
- Update `Situation` to link to `Topic` instead of directly to `LearningPath`
- Add OpenRouter LLM integration service for path generation
- Create onboarding survey API endpoint and service
- Create frontend onboarding survey wizard (multi-step form)
- Create frontend learning path dashboard with topics, situations, and chunks
- Add OpenRouter env vars to `docker-compose.yml` and `application.yml`
- Seed initial profession data

## Capabilities

### New Capabilities
- `onboarding-survey`: Multi-step survey flow collecting profession, level, goals, and context. Backend API to process survey and trigger LLM path generation.
- `llm-path-generation`: OpenRouter integration service that sends user profile to LLM and parses structured JSON response into Topics → Situations → Chunks → SubPhrases.
- `learning-path-dashboard`: Frontend pages displaying the generated learning path with topic cards, situation cards (with progress/level badges), and chunk detail view.

### Modified Capabilities
- `data-model`: Add Topic entity, SubPhrase entity, update Situation FK from LearningPath to Topic. Add level field to Situation.

## Impact

- **Database**: New tables `topics`, `sub_phrases`. Modified FK on `situations` table.
- **Backend**: New entities, repositories, DTOs, services (`LlmService`, `OnboardingService`), controllers
- **Frontend**: New pages (survey wizard, learning path dashboard, situation detail)
- **Dependencies**: Add Spring WebFlux/WebClient for OpenRouter HTTP calls
- **API**: New endpoints under `/api/onboarding/*` and `/api/learning-paths/*`
