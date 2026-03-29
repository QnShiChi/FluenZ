## 1. Setup and Initialization

- [x] 1.1 Extract existing standalone chunk components (e.g. upload helpers, API calls) from `AdminPage.tsx` into separate internal helpers if needed, to reduce clutter before the rewrite.
- [x] 1.2 Add necessary state variables to `AdminPage.tsx` (`selectedNodeId`, `selectedNodeType` which can be `'topic' | 'situation' | null`). Remove obsolete specific Sheet states (like `addingChunkSituationId`).

## 2. Left Panel: Tree View Component

- [x] 2.1 Refactor the left column (`w-[280px]` or `360px`) to display the Catalog Versions at the top, and the Topic -> Situation tree below it.
- [x] 2.2 Implement the Topic loop using `<Accordion>` for the Top-level folders (Topics).
- [x] 2.3 Inside each Topic Accordion, map the `situations` as simple clickable buttons (not nested accordions).
- [x] 2.4 Apply active highlighting (`bg-sky-50` etc) to the currently selected Situation or Topic button.

## 3. Right Panel: Workspace Component

- [x] 3.1 Implement the Topic Setting Workspace (renders when a Topic is selected): Shows Topic Name editing form and statistics.
- [x] 3.2 Implement the Situation Workspace (renders when a Situation is selected): Shows Situation Name, Level, Description, and Thumbnail editing form.
- [x] 3.3 Inside the Situation Workspace, render the flat list of Chunks.
- [x] 3.4 Inside the Chunk cards, render the flat list of SubPhrases.

## 4. Flattening Forms (Removing Sheets)

- [x] 4.1 Convert the "Add Chunk" Radix `Sheet` into an inline form at the bottom or top of the Situation Workspace.
- [x] 4.2 Convert the "Edit Chunk" Radix `Sheet` into an inline flip/toggle card (where the chunk display swaps to an edit grid).
- [x] 4.3 Apply the same inline expanding form pattern for "Add Subphrase" and "Edit Subphrase".

## 5. Verification & Polish

- [ ] 5.1 Test deeply nested interactions (adding a chunk, expanding its subphrases) to ensure the master `min-h-screen` viewport scrolls natively without artificial `overflow-hidden` blocks.
- [ ] 5.2 Test Topic and Situation creation still working securely in the new layout.
