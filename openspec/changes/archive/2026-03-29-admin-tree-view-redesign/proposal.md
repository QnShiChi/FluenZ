## Why

The current nested Accordion and Sheet UI for editing the entire version tree (Topics -> Situations -> Chunks -> SubPhrases) is causing severe scroll fatigue and UX bugs (e.g., hidden overflow clipping content, scrolling lock from unmounting Radix components). We need a completely flat, non-nested UI approach to resolve these issues and make managing large catalogs manageable and fast.

## What Changes

- Completely redesign `AdminPage.tsx` interface to use a Tree View + Workspace layout.
- **Left Column**: A collapsible Tree View (Explorer style) showing `Topic -> Situation`.
- **Right Column (Workspace)**: A dedicated, full-screen detail view that changes based on what is selected in the Tree View.
- When a Situation is selected in the Tree View, the Workspace displays its Chunks and SubPhrases in a flat, scrollable list explicitly isolated from the tree hierarchy.
- **BREAKING**: Remove overlapping/nested Accordions and `Sheet` overlays for typical item editing where in-place editing in the Workspace makes more sense. (We may still use Sheets for adding new Topics/Situations).

## Capabilities

### New Capabilities
- `admin-tree-view`: A layout mechanism to display a hierarchical side-nav for catalog entities.
- `admin-workspace-view`: A detail pane that mounts the relevant forms (Topic settings, Situation settings, or Chunks/Subphrases) dependending on the active node.

### Modified Capabilities
- `admin-catalog-management`: Changes from a deeply nested Accordion approach to a flat Master-Detail (Tree-Workspace) approach.

## Impact

- `frontend/src/pages/AdminPage.tsx` will be completely overhauled.
- Radix UI `Accordion` components may be swapped out for a custom Tree component or simplified Lists.
- State management for editing specific entities will change from multiple localized `addingChunkSituationId` etc., to a single `selectedNodeId` or `selectedNodeType` driving the Workspace.
