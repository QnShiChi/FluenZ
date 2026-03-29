## Context

The `AdminPage.tsx` currently renders an entire catalog version in a single view. The JSON structure returned from the API includes `CatalogPreview` > `Topic` > `Situation` > `Chunk` > `SubPhrase`. Because all relations are loaded and displayed as expanded cards simultaneously, the page becomes prohibitively long. Moreover, the state (`topicForms`, `situationDrafts`, etc.) currently lives in a giant unmounted state within the top-level `AdminPage` component.

## Goals / Non-Goals

**Goals:**
- Improve clarity by presenting only high-level details until the user drills down.
- Maintain scroll position when editing deeply nested items by using overlay components (Sheets).
- Refactor the code to improve modularity of the UI forms (optional but highly recommended as part of the React component tree).

**Non-Goals:**
- Changing the backend data structures or entity relational schemas.
- Adding pagination to the catalog view (assuming a single catalog version is reasonably sized when collapsed).

## Decisions

**Decision 1: Use Accordion for Hierarchical Navigation**
- *Rationale*: `shadcn/ui` Accordions offer smooth transitions and manage expanded/collapsed states natively. Wrapping each Topic and each Situation in an Accordion will keep the initial view very compact.

**Decision 2: Use Sheet for Edit/Create Forms**
- *Rationale*: A `Sheet` sliding from the right edge is standard for data-heavy administrative interfaces (e.g., Shopify, Stripe admin). It provides plenty of vertical space for long forms (like Chunk or SubPhrase descriptions and image uploads) without obstructing the parent context entirely or taking the user away from the page.

**Decision 3: Maintain State in Parent or Lift to Context?**
- *Rationale*: For now, state can stay in `AdminPage` or inside localized state blocks for each Sheet if they send updates directly to the API via props callbacks.

## Risks / Trade-offs

- **Risk: State Management Complexity** → The top-level `AdminPage` currently holds all draft states in massive records (`topicDrafts: Record<string, TopicFormState>`). Moving editing to a `Sheet` might require passing these states down. 
*Mitigation*: We can localize form state inside the Sheet component itself. The Sheet will receive the initial data as a prop, keep local state, and call `onSave` to trigger the API request and refresh the parent version without needing massive `Record<string, FormState>` objects at the root.
