## Context

FluenZ uses visual association (image-per-subphrase) as a core learning method. Currently, `ImageService` fetches images synchronously from Unsplash during path creation. Two problems exist:

1. **Situation thumbnails** are never populated — `LlmSituation` has no `imageKeyword` field, so `thumbnailUrl` is always `null`
2. **SubPhrase images** are unreliable — Unsplash free tier (50 req/hr) rate-limits, and some keywords return no results

With planned scale to 15–20 topics per path (~200–280 images), the current synchronous single-provider approach is unviable.

### Current Flow
```
LLM generates path → OnboardingService loops subphrases → ImageService.fetchImageUrl() per subphrase (blocking) → save all → return response
```

## Goals / Non-Goals

**Goals:**
- Every SubPhrase and Situation gets an image reliably (near 100% coverage)
- Path creation remains fast — user sees the path immediately, images load progressively
- Cost stays at $0 (free API tiers only)
- System scales to 200+ images per path without hitting rate limits

**Non-Goals:**
- AI-generated images (DALL-E, Stable Diffusion) — too slow and expensive at scale
- Real-time image search in frontend — all fetching stays server-side
- Image moderation/review workflow

## Decisions

### 1. Multi-provider fallback chain over single provider

**Decision**: Chain Unsplash → Pexels → Pixabay (combined ~350 req/hr free).

**Alternatives considered**:
- Single provider with retry: Still rate-limited to 50/hr
- AI image generation: $0.04/image × 250 = $10/path, 5-15s/image latency

**Rationale**: Three free providers give 7× the throughput with zero cost and high-quality stock photos that are better for visual association than AI-generated images.

### 2. Database-backed keyword cache

**Decision**: New `image_cache` table mapping `keyword → imageUrl` with TTL. Check cache before calling any provider.

**Rationale**: Many subphrases across users share similar keywords (e.g., "office meeting", "laptop work"). Caching avoids redundant API calls and makes subsequent path generations near-instant for cached keywords. Estimated cache hit rate after 10 paths: ~40-60%.

**Schema**:
```sql
CREATE TABLE image_cache (
    id UUID PRIMARY KEY,
    keyword VARCHAR(255) UNIQUE NOT NULL,
    image_url TEXT NOT NULL,
    provider VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP
);
```

### 3. Async image population via Spring @Async

**Decision**: Save the learning path immediately with `imageUrl = null`, return response to user, then populate images in a `@Async` background method.

**Alternatives considered**:
- Message queue (RabbitMQ/Kafka): Over-engineered for this use case
- Scheduled batch job: Adds unnecessary delay
- `CompletableFuture` inline: Still blocks the HTTP thread partially

**Rationale**: Spring `@Async` is simple, already in the stack, and provides true fire-and-forget background execution. Frontend already handles `null` image URLs with gradient fallback.

### 4. Situation imageKeyword in LLM prompt

**Decision**: Add `imageKeyword` field to `LlmSituation` DTO and update the LLM prompt to generate a 2–4 word stock photo keyword per situation.

**Rationale**: Minimal change — just add one field to the DTO and one line to the prompt. The same image provider chain handles both situation and subphrase images.

### 5. Parallel fetching within the async job

**Decision**: Use `CompletableFuture.allOf()` inside the async method to fetch all images concurrently (bounded by a thread pool of 10).

**Rationale**: Sequential fetching of 250 images at ~300ms each = ~75s. Parallel with 10 threads = ~8s. The thread pool prevents overwhelming external APIs.

## Risks / Trade-offs

- **External API dependency** → Mitigation: 3 providers + cache means even if 2 are down, images still get populated. Worst case: images stay null and frontend shows gradient placeholder.
- **Cache staleness** → Mitigation: TTL of 30 days. Stock photos don't change URLs frequently. Can be refreshed on cache miss.
- **API key management** → Mitigation: Add Pexels and Pixabay keys to application properties with blank defaults (graceful skip like current Unsplash).
- **Race condition on cache writes** → Mitigation: Use `INSERT ... ON CONFLICT DO NOTHING` (upsert) semantics via JPA.
