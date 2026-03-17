## 1. Schema Extensions

- [x] 1.1 Create `Topic` entity (name, orderIndex, ManyToOne LearningPath, OneToMany Situations)
- [x] 1.2 Create `SubPhrase` entity (text, orderIndex, ManyToOne Chunk)
- [x] 1.3 Update `Situation` — change parent FK from `LearningPath` to `Topic`, add `level` (Level enum) field
- [x] 1.4 Update `Chunk` — add OneToMany to SubPhrase
- [x] 1.5 Create `TopicRepository`, `SubPhraseRepository`
- [x] 1.6 Create DTOs: `TopicResponse`, `SubPhraseResponse`, updated `SituationResponse`, updated `ChunkResponse`
- [x] 1.7 Create `OnboardingRequest` DTO (professionId, communicationContexts, specificGoals)

## 2. Backend Configuration

- [x] 2.1 Add OpenRouter config to `application.yml`
- [x] 2.2 Add OpenRouter env vars to `docker-compose.yml` backend service
- [x] 2.3 Create `DataSeeder` component to seed professions on startup

## 3. LLM Integration

- [x] 3.1 Create `LlmService` with OpenRouter REST call and JSON parsing
- [x] 3.2 Design and implement the LLM prompt template
- [x] 3.3 Create `LlmResponseDto` for parsing LLM JSON output

## 4. Backend Services & Controllers

- [x] 4.1 Create `OnboardingService` (process survey → call LLM → save path)
- [x] 4.2 Create `OnboardingController` with `POST /api/onboarding/generate`
- [x] 4.3 Create `ProfessionController` with `GET /api/professions`
- [x] 4.4 Create `LearningPathController` with GET active path, topics, situations, chunks endpoints
- [x] 4.5 Update `SecurityConfig` to permit new endpoints for authenticated users

## 5. Frontend — Survey Wizard

- [x] 5.1 Create OnboardingPage with 4-step wizard UI
- [x] 5.2 Install Shadcn UI components needed (select, checkbox, textarea, progress)
- [x] 5.3 Create `useOnboardingStore` Zustand store for survey state

## 6. Frontend — Learning Path Dashboard

- [x] 6.1 Create DashboardPage with path header and topic/situation cards
- [x] 6.2 Create SituationDetailPage with chunks and sub-phrases
- [x] 6.3 Update React Router with new routes (/onboarding, /dashboard, /situations/:id)
- [x] 6.4 Update post-login redirect logic (no path → /onboarding, has path → /dashboard)

## 7. Verification

- [x] 7.1 Rebuild Docker images and verify backend starts
- [x] 7.2 Verify professions are seeded in DB
- [x] 7.3 Test full flow: survey → LLM generation → path saved → dashboard shows content
- [x] 7.4 Verify path retrieval endpoints return correct nested data
- [x] 7.5 Test frontend survey wizard and dashboard in browser
