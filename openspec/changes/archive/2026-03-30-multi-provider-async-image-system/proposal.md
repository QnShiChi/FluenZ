## Why

Personalized learning paths rely on visual association (image-per-subphrase) as a core pedagogical feature — images help learners form language-visual connections that improve retention. Currently, the system uses a single Unsplash API to fetch images by keyword, but this approach has two critical failures:

1. **Situation thumbnails are never populated** — the LLM does not generate image keywords for situations, so `thumbnailUrl` is always `null`
2. **SubPhrase images are unreliable** — Unsplash's free tier rate limit (50 req/hour) causes partial failures, and some keywords return no results

With the planned scale increase to 15–20 topics per path (~200–280 images needed), the single-provider synchronous approach will completely break.

## What Changes

- Add **Pexels** and **Pixabay** as fallback image providers alongside Unsplash (total ~350 req/hour free)
- Introduce a **global keyword→URL cache** (database-backed) to avoid redundant fetches across users
- Make image fetching **asynchronous** — save the learning path immediately with `imageUrl = null`, then populate images via a background job
- Extend the **LLM prompt** to generate `imageKeyword` for each Situation (for situation thumbnails)
- Add a frontend mechanism to **progressively load images** as they become available after path creation

## Capabilities

### New Capabilities
- `image-provider-chain`: Multi-provider image fetching with fallback chain (Unsplash → Pexels → Pixabay) and global keyword cache
- `async-image-population`: Background job system that populates images after learning path creation, decoupling image fetching from path generation

### Modified Capabilities
- None — existing specs are not changed at the requirement level

## Impact

- **Backend**: `ImageService` refactored into multi-provider chain; new `ImageCacheRepository` entity; new async job or `@Async` service method; `LlmService` prompt updated for situation image keywords; `OnboardingService` decoupled from synchronous image fetching
- **Frontend**: Dashboard and practice UI need to handle `null` image gracefully and refresh/poll when images become available
- **Database**: New `image_cache` table for keyword→URL mapping
- **External APIs**: New API keys needed for Pexels and Pixabay (both free)
