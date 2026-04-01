## 1. Orchestration

- [ ] 1.1 Add durable generation phases for `GENERATING_BLUEPRINT`, `GENERATING_CORE`, `PARTIALLY_PUBLISHED`, `GENERATING_REMAINING_CORE`, `GENERATING_ENRICHMENT`, and `COMPLETE`
- [ ] 1.2 Refactor `OnboardingService` to separate blueprint, publishable-core generation, background remaining-core generation, and enrichment
- [ ] 1.3 Add publish-window configuration for the first 4-6 topics
- [ ] 1.4 Persist `publishedTopicCount` separately from total generated topic progress

## 2. Core Content Contracts

- [ ] 2.1 Define a smaller core-content DTO/schema for publishable situations
- [ ] 2.2 Split optional fields into a separate enrichment contract
- [ ] 2.3 Update prompts so each core-content call generates exactly one situation with exactly 5 chunks

## 3. Parallel Generation

- [ ] 3.1 Implement bounded concurrency worker orchestration for situation-level generation
- [ ] 3.2 Add safe concurrency configuration with provider-aware defaults
- [ ] 3.3 Ensure progress reporting reflects partially published and background-completing states

## 4. Validation And Repair

- [ ] 4.1 Add core validation at situation/chunk granularity
- [ ] 4.2 Add chunk-level repair prompts for isolated failures
- [ ] 4.3 Add situation-level retry with low retry caps
- [ ] 4.4 Prevent batch-level regeneration except as a last-resort operational fallback

## 5. Progressive Publish

- [ ] 5.1 Persist and expose partially published personalized paths
- [ ] 5.2 Allow frontend/dashboard to load only already-published topics before full path completion
- [ ] 5.3 Continue generating remaining topics and enrichment in the background
- [ ] 5.4 Ensure refresh keeps showing the same published topics and continues background completion correctly

## 6. Generation Progress Route

- [ ] 6.1 Add a dedicated frontend route for personalized generation progress
- [ ] 6.2 Add backend response fields needed to show published topic count, generated topic count, and current phase durably
- [ ] 6.3 Allow the learner to move between dashboard and progress route without losing continuity

## 7. Metrics

- [ ] 7.1 Record phase timing metrics: blueprint, publishable core, remaining core, enrichment
- [ ] 7.2 Record parse failure rate, validation failure rate, retry rate, and publish time
- [ ] 7.3 Add operational logging for retry scope and repair scope
- [ ] 7.4 Record mismatch rate between `publishedTopicCount` and dashboard-rendered topic count

## 8. Rollout

- [ ] 8.1 Ship durable publish watermark first
- [ ] 8.2 Ship dashboard rendering against published topics only
- [ ] 8.3 Ship dedicated generation-progress route
- [ ] 8.4 Ship narrower retry and background enrichment refinements
- [ ] 8.5 Compare quality and latency against current baseline before raising concurrency further
