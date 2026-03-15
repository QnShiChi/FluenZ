## Context

FluenZ is a greenfield AI-powered language learning platform. There is currently no application code at all — only the OpenSpec configuration scaffolding exists. Before any feature development can begin, we need a working local development stack comprising a React frontend, Spring Boot backend, and PostgreSQL database, all containerized with Docker Compose and managed via Makefile. The developer also uses DBeaver for database inspection, so PostgreSQL must be accessible from the host.

## Goals / Non-Goals

**Goals:**
- A single `make up` command starts the entire dev stack (frontend, backend, postgres)
- PostgreSQL is accessible from the host on port `5432` for DBeaver connections
- Backend project follows Clean Architecture with proper layer separation from day one
- Frontend project has Tailwind CSS, Shadcn UI, and Zustand ready to use
- Database backups and restores are one-command operations via Makefile
- Each service has its own Dockerfile suitable for local development

**Non-Goals:**
- Production-ready deployment configuration (CI/CD pipelines, cloud infra)
- Implementing any application features (onboarding, chunking, roleplay)
- Database schema/migrations (no tables yet — just the empty database)
- Authentication, API design, or business logic
- Production multi-stage frontend builds (dev server is sufficient for now)

## Decisions

### 1. Project directory structure
**Decision:** Monorepo with `backend/` and `frontend/` at root level.
**Rationale:** Simplifies Docker Compose orchestration and Makefile management. Both services share the same `docker-compose.yml` and `.env`. Alternative (separate repos) adds unnecessary complexity for a single-team project.

### 2. Gradle over Maven
**Decision:** Use Gradle (Kotlin DSL) for the Spring Boot backend.
**Rationale:** User preference. Gradle offers faster incremental builds and more concise configuration. We'll use the Gradle wrapper (`gradlew`) inside the Docker container for reproducibility.

### 3. Zustand over Redux
**Decision:** Use Zustand for frontend global state management.
**Rationale:** User preference. Zustand is lighter, has less boilerplate, and is well-suited for the expected state complexity (user profile, learning path, UI state).

### 4. PostgreSQL port mapping
**Decision:** Map container port `5432` to host port `5432`.
**Rationale:** Standard PostgreSQL port allows DBeaver to connect with default settings. If the developer has a local PostgreSQL instance, they can adjust the host port in `.env`.

### 5. Environment variable management
**Decision:** Use a `.env` file at the project root, referenced by `docker-compose.yml`.
**Rationale:** Centralizes all configuration (DB credentials, ports, API keys). A `.env.example` is committed to git; `.env` is gitignored. This prevents accidental credential leaks while keeping setup easy.

### 6. Backend Dockerfile strategy
**Decision:** Multi-stage Dockerfile — Stage 1: Gradle build with JDK 17, Stage 2: JDK 17 slim runtime.
**Rationale:** Keeps the runtime image small. For local dev, we'll use `docker compose watch` or volume mounts with a dev profile for hot-reload capability.

### 7. Frontend Dockerfile strategy
**Decision:** Single-stage Node.js 18 image running `npm run dev` with Vite's dev server.
**Rationale:** In development, we want HMR (Hot Module Replacement). No need for a production build stage yet. Vite's dev server is proxied through the exposed port.

### 8. Shadcn UI initialization
**Decision:** Initialize Shadcn UI via `npx shadcn@latest init` inside the frontend project, with the "new-york" style and neutral color palette.
**Rationale:** Shadcn UI is not an npm package — it generates component files directly. Initializing it during scaffolding ensures the `components.json` config and base components are ready.

## Risks / Trade-offs

- **Port conflict on 5432** → Mitigation: Document in README that users with local PostgreSQL should change `POSTGRES_HOST_PORT` in `.env`. Provide `.env.example` with configurable port.
- **Gradle build time in Docker** → Mitigation: Use Gradle build cache and dependency caching in Dockerfile layers. First build is slow; subsequent builds are fast.
- **Shadcn version drift** → Mitigation: Pin to `@latest` at scaffolding time; components are source-owned so version drift doesn't break existing code.
- **Docker Compose version compatibility** → Mitigation: Use Compose file format `3.8` which is widely supported. Document minimum Docker version in README.
