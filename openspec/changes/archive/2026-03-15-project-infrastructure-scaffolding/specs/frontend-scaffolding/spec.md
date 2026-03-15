## ADDED Requirements

### Requirement: React project with Vite and TypeScript
The system SHALL provide a React project at `frontend/` initialized with Vite and TypeScript. The project SHALL use Node.js 18+.

#### Scenario: Dev server starts
- **WHEN** developer runs `npm run dev` inside the `frontend/` directory
- **THEN** the Vite dev server starts and is accessible at `http://localhost:5173`

---

### Requirement: Tailwind CSS integrated
The frontend project SHALL have Tailwind CSS installed and configured. All components SHALL be able to use Tailwind utility classes for styling.

#### Scenario: Tailwind classes work
- **WHEN** a developer uses Tailwind classes like `bg-blue-500` in a component
- **THEN** the classes are compiled and applied in the browser

---

### Requirement: Shadcn UI initialized
The frontend project SHALL have Shadcn UI initialized with the "new-york" style and a neutral color palette. The `components.json` configuration file SHALL be present. The `cn()` utility function SHALL be available at `src/lib/utils.ts`.

#### Scenario: Shadcn components can be added
- **WHEN** developer runs `npx shadcn@latest add button`
- **THEN** the Button component is created in the local components directory and is importable

---

### Requirement: Zustand configured for state management
The frontend project SHALL have Zustand installed as a dependency. A sample store file SHALL exist at `src/stores/useAppStore.ts` as a reference for creating new stores.

#### Scenario: Zustand store works
- **WHEN** a component imports and uses the app store
- **THEN** state reads and updates function correctly

---

### Requirement: Axios configured for API communication
The frontend project SHALL have Axios installed. An API client SHALL be configured at `src/services/api.ts` with the base URL pointing to the backend (`http://localhost:8080/api`).

#### Scenario: API client configured
- **WHEN** a service function uses the API client to call the backend
- **THEN** the request is sent to the correct base URL with proper headers

---

### Requirement: Frontend Dockerfile for development
The frontend SHALL have a `Dockerfile` at `frontend/Dockerfile` using `node:18-alpine`. It SHALL install dependencies and run the Vite dev server with `--host 0.0.0.0` to be accessible from outside the container.

#### Scenario: Frontend container starts
- **WHEN** the frontend Docker container starts
- **THEN** the Vite dev server is accessible from the host at `http://localhost:5173`

---

### Requirement: Project structure follows separation of concerns
The frontend project SHALL organize code into:
- `src/components/` — Reusable UI components
- `src/hooks/` — Custom React hooks
- `src/services/` — API service modules (using Axios)
- `src/stores/` — Zustand state stores
- `src/pages/` — Page-level components
- `src/lib/` — Utility functions

#### Scenario: Directory structure exists
- **WHEN** the project is scaffolded
- **THEN** all listed directories exist
