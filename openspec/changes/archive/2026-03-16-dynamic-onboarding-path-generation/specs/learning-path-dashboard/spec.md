## ADDED Requirements

### Requirement: Onboarding survey wizard
The frontend SHALL provide a multi-step survey at `/onboarding` with 4 steps:
1. **Profession** — select from list or type custom profession
2. **Level** — confirm/change current level (BEGINNER, INTERMEDIATE, ADVANCED)
3. **Context** — select communication contexts (checkboxes: daily meetings, presentations, emails, code reviews, client calls, etc.)
4. **Goals** — optional free text describing specific goals

The wizard SHALL show progress indicators and allow back navigation.

#### Scenario: Complete survey
- **WHEN** a user completes all 4 steps and submits
- **THEN** the survey data is sent to `POST /api/onboarding/generate`
- **AND** a loading animation is shown while the LLM generates content (3-10 seconds)
- **AND** the user is redirected to the learning path dashboard on success

---

### Requirement: Learning path dashboard
The frontend SHALL provide a dashboard at `/dashboard` showing:
- Path header: profession name, level badge, topic count, situation count
- Topic sections with topic name as header
- Situation cards under each topic showing: title, description, level badge, progress (X/Y chunks), thumbnail
- Click on a situation card navigates to situation detail

#### Scenario: View dashboard
- **WHEN** an authenticated user with an ACTIVE learning path visits `/dashboard`
- **THEN** all topics and situations are displayed with their metadata

---

### Requirement: Situation detail page
The frontend SHALL provide a detail page at `/situations/:id` showing:
- Situation title and description
- Level badge
- List of chunks with: phrase (English), translation (Vietnamese)
- Each chunk expandable to show sub-phrases
- "Bắt đầu học" button (disabled placeholder for Module B)

---

### Requirement: Auto-redirect after login
After login/register, if the user has no ACTIVE learning path, they SHALL be redirected to `/onboarding`. If they have an ACTIVE path, they go to `/dashboard`.

---

### Requirement: Navigation updates
The frontend SHALL update React Router with new routes:
- `/onboarding` → Survey wizard (protected)
- `/dashboard` → Learning path dashboard (protected, new home)
- `/situations/:id` → Situation detail (protected)
