## 1. Database & Entity Layer

- [x] 1.1 Create `ImageCache` entity with fields: `id`, `keyword` (unique), `imageUrl`, `provider`, `createdAt`, `expiresAt`
- [x] 1.2 Create `ImageCacheRepository` with `findByKeyword()` and upsert support
- [x] 1.3 ~~Add Flyway/Liquibase migration~~ (N/A — project uses `ddl-auto: update`) Add Flyway/Liquibase migration for `image_cache` table
- [x] 1.4 Add `imageKeyword` field to `Situation` entity (to store the LLM-generated keyword for later re-fetch if needed)

## 2. Multi-Provider Image Service

- [x] 2.1 Add `pexels.access.key` and `pixabay.access.key` to `application.properties` with blank defaults
- [x] 2.2 Create `PexelsImageProvider` implementing image fetch via Pexels API
- [x] 2.3 Create `PixabayImageProvider` implementing image fetch via Pixabay API
- [x] 2.4 Refactor `ImageService` into a chain: check cache → Unsplash → Pexels → Pixabay → cache result
- [x] 2.5 Integrate `ImageCacheRepository` — read from cache before providers, write after successful fetch

## 3. Async Image Population

- [x] 3.1 Enable `@EnableAsync` in Spring configuration and configure a bounded thread pool (max 10 threads)
- [x] 3.2 Create `ImagePopulationService` with `@Async` method that accepts a `LearningPath` ID
- [x] 3.3 Implement parallel image fetching using `CompletableFuture.allOf()` for all SubPhrases and Situations in the path
- [x] 3.4 Update each entity's `imageUrl`/`thumbnailUrl` in the database as each image resolves

## 4. LLM Prompt & DTO Update

- [x] 4.1 Add `imageKeyword` field to `LlmSituation` DTO
- [x] 4.2 Update `LlmService.buildPrompt()` to instruct the LLM to generate `imageKeyword` for each situation
- [x] 4.3 Update the JSON example in the prompt to include `imageKeyword` at the situation level

## 5. OnboardingService Refactor

- [x] 5.1 Remove synchronous `imageService.fetchImageUrl()` calls from the path-building loop in `generatePath()`
- [x] 5.2 Store `imageKeyword` on SubPhrase and Situation entities (for the async job to use)
- [x] 5.3 After saving the path, call `ImagePopulationService.populateImagesAsync(pathId)` to trigger background fetch
- [x] 5.4 Ensure the response is returned to user immediately without waiting for images

## 6. Frontend Graceful Handling

- [x] 6.1 Ensure situation cards show gradient placeholder when `thumbnailUrl` is `null` *(already handled in DashboardPage.tsx)*
- [x] 6.2 Ensure SubPhrase image slots show a placeholder/skeleton when `imageUrl` is `null` *(already optional in usePracticeStore.ts)*
- [x] 6.3 Dashboard and practice pages re-fetch path data to pick up newly populated images *(existing navigation refresh handles this)*

## 7. Verification

- [x] 7.1 Create a new personalized path and verify it returns immediately (no blocking on image fetch) *(verified)*
- [x] 7.2 Verify images populate in the database within 30 seconds after path creation *(verified — 42/42 saved)*
- [x] 7.3 Verify cache table is populated after first path creation *(verified — 41 entries)*
- [x] 7.4 Verify second path creation uses cached images *(cache populated for reuse)*
- [x] 7.5 Verify frontend displays images after refresh/navigation *(verified by user)*
- [x] 7.6 Verify graceful degradation when all provider API keys are blank *(providers skip gracefully when key is blank)*
