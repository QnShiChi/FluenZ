## Context

Currently, the Admin Catalog page (`AdminPage.tsx`) uses a Deep Nested Accordion approach (Topic -> Situation -> Chunks -> SubPhrases) along with Radix UI `Sheet` overlays for editing items inline. As catalogs grow and chunks become more complex, this pattern causes several UX issues:
1. **Vertical Scroll Fatigue**: Users have to scroll continuously to navigate deep hierarchies.
2. **Scroll Lock Bugs**: The interaction between nested Accordions and unmounted Radix `Sheet` components occasionally leaves the DOM `body` in a scroll-locked state (`pointer-events: none; overflow: hidden`), trapping the user.
3. **Visibility Limits**: Due to `AccordionContent` height recalculation issues, adding dynamic content expands containers incorrectly or clips items out of view.

## Goals / Non-Goals

**Goals:**
- Replace the nested Accordion UI with a **Master-Detail (Tree & Workspace) Architecture**.
- Eliminate scroll-lock bugs by removing overlapping Radix UI `Sheet` components for internal edits (Chunks/SubPhrases).
- Provide a clean, permanent workspace view where Chunks and SubPhrases can grow infinitely without clipping.

**Non-Goals:**
- Completely overhauling the backend API (this is purely a frontend UI/UX transformation).
- Changing the actual data schema (Topics, Situations, Chunks, SubPhrases remain exactly the same).

## Decisions

1. **Two-Column Layout (`grid-cols-[280px_1fr]`)**
   - **Left Panel (Tree View)**: Displays Topics and their Situations. Accordions can still be used here to expand a Topic and list its Situations, but they will NOT contain Chunks or Subphrases.
   - **Right Panel (Workspace View)**: Displays the properties of whatever node is selected in the Tree View.
   - *Rationale*: A classic IDE/Folder view guarantees that the user always has constant context of where they are without deeply nested scrolling.

2. **State Management Migration (`selectedNode`)**
   - Currently, state is tracked via `editingTopicId`, `editingSituationId`, `addingChunkSituationId`, etc.
   - This will be normalized to a simple active selection state: e.g., `activeSituationId`. When a user clicks a Situation in the left panel, the right Workspace completely re-renders to show that Situation's details and Chunks.

3. **In-place Flat Forms instead of Sheets**
   - Editing Chunks and SubPhrases will be done via inline/flat cards inside the Workspace instead of opening sliding `Sheet` drawers. This prevents Z-index stacking collisions and scroll bugs.

## Risks / Trade-offs

- **[Risk] State Loss During Navigation** → Users might click away from a Situation while halfway through editing a Chunk.
  *Mitigation*: Implement a "Are you sure you want to discard unsaved changes?" prompt, or auto-save robustly in the component state until explicit save.
- **[Risk] Responsive Design (Mobile Support)** → A two-column master-detail view breaks on small screens.
  *Mitigation*: Given this is an Admin Portal, desktop is the primary target. We can hide the left sidebar behind a generic Hamburger menu on mobile if needed.
