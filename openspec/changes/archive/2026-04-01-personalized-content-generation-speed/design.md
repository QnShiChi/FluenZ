## Context

The current Personalized Learning content pipeline has one strong property and one major weakness:

- strong: content quality is already fairly good
- weak: the learner waits too long because detail generation is still too coarse and too serial

Today the backend roughly does:

1. generate a learner blueprint and topic roadmap
2. generate detailed topic content in batches
3. validate the entire batch
4. retry the entire batch when parsing or validation fails
5. split failing batches into smaller batches
6. save the path only after a large amount of detail work is complete

This architecture preserves quality because prompts are rich and context-heavy, but it is slow because:

- each detail call asks for too much nested JSON
- parse failures waste large amounts of work
- validation failure at a small unit forces retry at a much larger unit
- progress is tied to oversized generation units
- publish is delayed until too much non-critical work is complete

## Goals

- deliver usable personalized content within 2-3 minutes from `Generate My Path`
- preserve current quality characteristics: persona fit, natural communication tone, reusable chunks, and progression depth
- keep final state at 20-25 topics with complete content
- publish core content early and enrich the rest in the background
- ensure anything reported as published is truly visible and learnable in the dashboard
- allow the learner to either start learning immediately or open a dedicated generation-progress route
- preserve generation continuity across refreshes and route changes
- make the pipeline easier to debug, retry, and measure

## Non-Goals

- reducing topic count for speed
- making content generic or template-heavy
- weakening subphrase quality rules
- waiting for all enrichment before the learner can start

## Why Current Quality Is Good

Current quality is mainly driven by four good ingredients that must be preserved:

1. the blueprint stage gives the model a coherent learner persona and progression arc
2. prompts are specific about communication usefulness and chunk structure
3. validation rejects structurally weak outputs
4. generation is still personalized by role, industry, context, goals, and pain points

The redesign therefore should not remove those ingredients. It should preserve them while shrinking the amount of work per call and the blast radius of each failure.

## Root Cause Of Latency

The dominant bottleneck is `detail content generation`, not image hydration and not blueprint creation.

The biggest time sinks are:

1. oversized structured LLM calls
2. coarse retry boundaries
3. repeated parse failures on large JSON payloads
4. serial wall-clock accumulation across many detail batches
5. generating non-core fields too early

In other words, the system is spending too much time forcing the model to produce large, perfect, all-fields-included JSON responses before the user is allowed to see anything useful.

## Recommended Architecture

### Phase A: Blueprint And Topic Planning

Keep a strong initial blueprint call that produces:

- persona summary
- communication priorities
- 20-25 topics in order
- topic intent/goal notes

This phase remains central for quality because it defines the roadmap and prevents generic drift.

### Phase B: Publishable Core Topic Generation

Generate only the first publish window of the path for immediate use.

Recommended publish window:

- first 4-6 topics
- each with situations and exactly 5 chunks
- each chunk with:
  - root sentence
  - translation if needed by the current learning flow
  - context/question if needed by the current learning flow
  - exactly 3 usable subphrases

Do not require `ipa`, `distractors`, or auxiliary image fields in this phase.

This is the main lever that enables 2-3 minute usable publish without quality collapse.

### Phase C: Background Core Completion

After the first publish window is ready, continue generating the remaining topics in the background with the same core-content contract.

The learner is already unblocked, while the system finishes the rest of the path.

### Phase D: Enrichment

After core content exists, enrich missing optional fields:

- ipa
- distractors
- imageKeyword
- other non-blocking support fields

This phase should run asynchronously and must never block the learner from starting.

### Phase E: Targeted Repair

If validation fails:

- repair only the failing chunk
- if chunk-level repair repeatedly fails, repair only the situation
- regenerate a topic only if multiple situations in that topic fail

Large batch retries are explicitly disallowed in the new design except as a last-resort operational fallback.

## Generation Units

### Blueprint unit

- one call
- roadmap quality first

### Topic skeleton unit

Optional internal planning layer per topic:

- topic goal
- situation list
- intended difficulty
- language focus

This can be created together with blueprint or as a lightweight second stage.

### Situation core-content unit

Recommended primary detail unit:

- one situation per call
- all 5 chunks generated together

Why this is the best balance:

- enough local context to keep chunk quality coherent
- much smaller than multi-topic nested JSON
- narrower repair radius than topic-level generation
- still preserves persona/topic context quality

Chunk-level generation is even smaller but risks weakening intra-situation coherence and increases orchestration overhead too much.

## Parallelization Strategy

Parallelize at the `situation generation` layer.

Recommended operational model:

- generate the first publish window with bounded concurrency
- concurrency target: 4-8 in-flight situation jobs depending on provider limits
- once publish window is complete enough, persist and publish the usable path
- continue background generation of remaining situations with the same bounded worker pool

Why this works:

- smaller calls reduce parse failure rate
- multiple in-flight situation calls reduce wall-clock time
- each situation still has enough context to stay coherent

## Structured Output Redesign

Use two separate contracts:

### Core contract

Small, strict, publishable:

- situation title
- situation description
- level
- exactly 5 chunks
- per chunk:
  - contextQuestion or equivalent trigger if required by current flow
  - contextTranslation when required
  - rootSentence
  - rootTranslation when required
  - exactly 3 subphrases
  - subphrase translation when required

### Enrichment contract

Generated later:

- ipa
- distractors
- imageKeyword
- any additional helper assets

This reduces parse payload size and improves schema stability in the critical path.

## Validation Redesign

Validation must be layered:

### Core validation

Runs before publish:

- topic count within roadmap contract
- situation has exactly 5 chunks
- chunk has exactly 3 subphrases
- subphrases are usable phrases, not isolated tokens
- root sentence and phrases are non-empty and structurally coherent

### Enrichment validation

Runs after publish:

- ipa present or repairable
- distractors valid
- image keyword present

Publish is blocked only by core validation, never by enrichment validation.

## Retry Strategy

### Situation-level retry

If a situation fails parse or core validation:

- retry the same situation with the same topic context and a compact repair prompt
- maximum small retry count per situation: 2

### Chunk-level repair

If only one chunk fails after parse:

- repair only that chunk
- preserve the rest of the situation

### Enrichment retry

Optional fields get their own independent retries later.

This redesign drastically reduces wasted regeneration.

## Publish Model

The system should support:

- `GENERATING_BLUEPRINT`
- `GENERATING_CORE`
- `PARTIALLY_PUBLISHED`
- `GENERATING_REMAINING_CORE`
- `GENERATING_ENRICHMENT`
- `COMPLETE`
- `FAILED`

The learner may enter the product when the path is `PARTIALLY_PUBLISHED`, as long as the first publish window is coherent and learnable.

## Durable Publish State

Progressive publish must be durable rather than inferred only from in-memory progress.

The backend should persist enough state to answer two separate questions correctly even after refresh or restart:

1. how many topics have been generated in total
2. how many topics have actually been published and are safe to show in the dashboard

Recommended durable fields:

- `publishedTopicCount`
- `generatedTopicCount`
- `generationPhase`
- `lastPublishedAt`
- `backgroundCompletionStatus`

The dashboard must render only `publishedTopicCount` topics, never speculative or not-yet-persisted topics.

## Learner Experience Model

Once the first publish window is ready, the learner has two valid paths:

1. go straight to the dashboard and start learning the published topics immediately
2. open a dedicated generation-progress route to watch the remaining topics complete

Both routes must remain consistent with the same durable publish state.

### Dashboard behavior

- if `publishedTopicCount > 0`, the learner can study immediately
- only already-published topics are shown
- as more topics are published in the background, the dashboard reflects them after refresh or live polling

### Generation progress route behavior

- shows `publishedTopicCount`, `generatedTopicCount`, and total topic count
- shows whether the path is `PARTIALLY_PUBLISHED`, `GENERATING_REMAINING_CORE`, `GENERATING_ENRICHMENT`, or `COMPLETE`
- lets the learner leave and come back without losing generation continuity
- remains available until the path reaches `COMPLETE`

## Redirect Semantics

Redirecting away from onboarding is allowed as soon as the path is truly partially published, but only if:

- the publish window is already committed to durable storage
- the dashboard can immediately load and show those published topics
- the generation-progress route is available for users who prefer to monitor completion instead of studying right away

This means the system must no longer treat `generatedTopicCount` as equivalent to `publishedTopicCount`.

## Recommended Rollout

### Phase 1

- split core vs enrichment
- publish first 4-6 topics early
- add path status for partial publish
- add durable `publishedTopicCount` and teach the dashboard to trust it instead of raw generation progress

### Phase 2

- move detail generation from multi-topic batch to situation-level units
- add concurrency-controlled worker pool
- add dedicated generation-progress route backed by durable publish state

### Phase 3

- add chunk-level repair and narrower retry
- add per-phase metrics and dashboards
- add refresh-safe polling/live updates for both dashboard and generation-progress route

### Phase 4

- optimize prompt templates, repair prompts, and provider-specific tuning

## Success Metrics

- `time_to_first_usable_path_p95 <= 3 minutes`
- `full_path_completion_p95 <= 10 minutes` initially, then lower over time
- `core_validation_failure_rate < 10%`
- `parse_failure_rate` drops materially versus current batch generation
- `average_retry_units_per_path` decreases
- `content_acceptance_rate` stays near current baseline
- `learner can start first lesson before full path completion`
- `publishedTopicCount` and dashboard-visible topic count stay consistent
- refresh does not reset or hide already published topics

## Final Recommendation

The best architecture is:

- keep a strong blueprint stage
- generate `core content` per situation with bounded parallelism
- publish the first 4-6 topics as soon as they are validated
- persist a durable publish watermark so the dashboard and monitoring route stay consistent
- finish remaining topics in the background
- enrich optional fields after publish
- validate and retry at chunk/situation granularity, not batch granularity

This is the best balance of speed, quality, maintainability, and debuggability. It preserves the ingredients that currently make the content good, while removing the oversized generation units and coarse retries that currently make the system slow.
