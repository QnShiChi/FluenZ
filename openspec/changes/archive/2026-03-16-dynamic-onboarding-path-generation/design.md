## Context

The FluenZ platform has auth, users, and a base data model (LearningPath → Situation → Chunk). Based on the reference app design, we need to add a Topic grouping layer, SubPhrase entities, and integrate with OpenRouter to auto-generate personalized learning paths from user survey data.

## Goals / Non-Goals

**Goals:**
- Multi-step onboarding survey collecting profession + context
- OpenRouter LLM integration to generate structured learning content
- Extended data model: Topic, SubPhrase, Situation.level
- Frontend survey wizard and learning path dashboard
- Seed initial profession list

**Non-Goals:**
- Practice/chunking flow (Module B — next change)
- AI roleplay (Module C)
- Text-to-speech / speech-to-text
- Progress tracking UI (comes with Module B)

## Decisions

### 1. Data Model: Add Topic layer
**Decision:** Insert `Topic` entity between `LearningPath` and `Situation`:
```
LearningPath → Topic → Situation → Chunk → SubPhrase
```
**Rationale:** The reference app groups situations into topics ("Báo Cáo Tiến Độ", "Trao Đổi Code"). Without Topic, situations are a flat list which is hard to navigate.

### 2. SubPhrase entity for fill-in-the-blank
**Decision:** Add `SubPhrase` entity under Chunk with fields: `text` (String), `orderIndex`.
**Rationale:** Each chunk has multiple sub-phrases to memorize (e.g., "due to new requirements", "because of user feedback"). These are the actual phrases users practice in Module B.

### 3. Situation gets a `level` field
**Decision:** Add `level` (Level enum) to Situation entity.
**Rationale:** Reference app shows BEGINNER/ADVANCED badges on each situation card. Different situations within the same topic can have different difficulty levels.

### 4. LLM via OpenRouter (OpenAI-compatible API)
**Decision:** Use Spring's `RestTemplate` to call OpenRouter's `/chat/completions` endpoint. Model: `google/gemini-2.0-flash-001`.
**Rationale:** OpenRouter provides a unified API for multiple LLM providers. RestTemplate is simpler than WebClient for synchronous calls. Gemini Flash is fast and cost-effective (~$0.1/1M tokens).

### 5. Structured JSON output from LLM
**Decision:** Prompt the LLM to return a strict JSON schema containing topics → situations → chunks → subPhrases. Parse with Jackson.
**Rationale:** Structured output makes parsing reliable. We validate the response and reject malformed data.

### 6. Survey questions (balanced UX vs detail)
**Decision:** 4-step survey:
1. Select profession (from seeded list or type custom)
2. Confirm current level (from profile, editable)
3. Select communication context (daily meetings, presentations, emails, etc.)
4. Describe specific goals (free text, optional)
**Rationale:** 4 steps provide enough context for LLM while keeping conversion rate high.

### 7. Profession seeding
**Decision:** Seed ~10 common professions via a `DataSeeder` component that runs on startup if table is empty.
**Rationale:** Users need profession options immediately. Can always add more later.

## Risks / Trade-offs

- **LLM response quality varies** → Mitigation: validate JSON structure, retry on failure, provide fallback
- **LLM latency (3-10s)** → Mitigation: show loading animation, generate async if needed later
- **Token cost** → Mitigation: Gemini Flash is cheap; cache generated paths per profession+level combo later
- **Schema migration** → Mitigation: `ddl-auto: update` handles new tables/columns. Situation FK change needs care — drop old FK, add new one via Hibernate
