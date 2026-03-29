## Why

The current Admin Catalog page displays all hierarchical data (Topics > Situations > Chunks > SubPhrases) in a fully expanded, single-page view. This creates an overwhelmingly long interface that requires excessive scrolling. Editing any item opens an inline form that drastically shifts the layout, forcing the user to lose their place and scroll significantly to find the save or cancel buttons. This change improves the UX by introducing collapsible sections and slide-out / modal editing forms to keep the interface compact and context-aware.

## What Changes

- Wrap the hierarchical data display (Topics, Situations, Chunks, SubPhrases) in Accordion or Collapsible components so only one section is expanded at a time.
- Replace inline edit/create forms with a Sheet (Sidebar Drawer) or Dialog (Modal) to isolate the editing context without shifting the main page layout.
- Refactor the massive `AdminPage.tsx` file by extracting forms and list items into smaller components.

## Capabilities

### New Capabilities
- `admin-catalog-ux`: Overhaul of the Admin Catalog UI components introducing Accordion and Sheet/Drawer for hierarchical data management.

### Modified Capabilities

## Impact

- Frontend ONLY: Modifies `AdminPage.tsx` and refactors it into smaller sub-components.
- Integrates new shadcn/ui components (`Accordion`, `Sheet`, `Dialog`).
- Backend API endpoints remain completely unchanged.
