## Why

The Personalized Learning pipeline now produces much better thumbnails and more stable content quality, but the biggest product problem remains unresolved: content generation still takes around 10-15 minutes before the learner can start.

The current detail generation stage is too slow because it relies on large structured LLM calls, coarse retry boundaries, and batch-level regeneration. This creates long wall-clock time even when most generated content is already acceptable.

The product goal is no longer "wait for the whole path to finish." It is:

- publish a usable personalized path within 2-3 minutes
- keep content quality close to current quality
- continue completing the rest of the path in the background
- avoid quality collapse from blunt prompt simplification

## What Changes

- Redesign personalized content generation into phased publishing:
  - roadmap/blueprint planning
  - publishable core topic generation
  - targeted validation
  - targeted repair
  - asynchronous enrichment
- Separate `core learning content` from `enrichment content`
- Introduce progressive content publish so the learner can begin as soon as the first high-quality portion of the path is ready
- Persist published-content state so the dashboard only exposes topics that are truly available to learn
- Add a dedicated generation-progress route so the learner can monitor path completion without being forced to stay on the onboarding screen
- Reduce each LLM call scope from coarse multi-topic detail batches to smaller generation units
- Add concurrency-controlled parallel generation for independent units
- Replace batch-level retry with situation/chunk-level retry and repair
- Tighten structured output contracts for the core content path
- Add durable generation state for partially published and background-completing paths
- Add performance metrics for time-to-usable-content, retry rate, and publish progress

## Capabilities

### New Capabilities

- `personalized-content-generation-performance`: Deliver publishable core personalized content within 2-3 minutes
- `progressive-content-publish`: Expose a usable partial path early, then fill in remaining topics in the background
- `targeted-content-repair`: Validate and regenerate only failing situations/chunks/enrichment units
- `progressive-generation-monitoring`: Let the learner track background topic generation on a dedicated route while already-published topics remain learnable

### Modified Capabilities

- `two-pass-generation`: Refined into multi-phase core-content-first generation
- `generation-content-validation`: Shift from coarse rejection to fine-grained validation and repair

## Impact

**Backend**
- `OnboardingService` orchestration changes from heavy detail batches to phased publish + background completion
- `LlmService` prompt architecture changes from large nested JSON batches to smaller scoped generation units
- New generation state and progress metadata for partially published paths
- New validation/repair workflow for chunk-level and situation-level failures

**Frontend**
- onboarding progress UI must distinguish `usable path ready` from `background completion`
- dashboard/path loading should tolerate partially completed personalized paths while remaining learnable
- new dedicated generation-progress route should show published topic count, remaining topic count, and current generation status
- dashboard should display only actually published topics and allow the learner to study them immediately

**Database**
- additional generation status metadata may be needed on `LearningPath`, `Topic`, `Situation`, and/or companion tables for:
  - publish readiness
  - published topic count / published content watermark
  - repair status
  - enrichment status
  - retry counters
  - phase timings
