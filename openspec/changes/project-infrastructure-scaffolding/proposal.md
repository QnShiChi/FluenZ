## Why

The FluenZ platform currently has no application code, infrastructure, or development environment. Before any feature work (onboarding, chunking practice, AI roleplay) can begin, we need a fully containerized, runnable project foundation. This includes scaffolded frontend and backend applications, a PostgreSQL database accessible via DBeaver for local development, and a Makefile to orchestrate the entire stack with simple commands.

## What Changes

- Create a **Spring Boot (Gradle, Java 17+)** backend project at `backend/` with base package `com.fluenz.api`, configured with Clean Architecture layers (controller, service, repository, dto, entity).
- Create a **React (Vite + TypeScript)** frontend project at `frontend/` with Tailwind CSS, Shadcn UI, and Zustand pre-configured.
- Create a **Dockerfile** for the backend (multi-stage: Gradle build → JDK runtime).
- Create a **Dockerfile** for the frontend (Node.js dev environment).
- Create a **docker-compose.yml** at the project root orchestrating 3 services: `frontend`, `backend`, `postgres`.
- Configure PostgreSQL with credentials (`fluenz_db` / `fluenz_user` / `fluenz_secret`), port `5432` exposed to host for DBeaver access, persistent volume, and `./backups` directory mapping.
- Create a **Makefile** with commands: `build`, `up`, `down`, `logs`, `clean`, `backup-db`, `restore-db`.
- Add a `README.md` with setup instructions including DBeaver connection details.

## Capabilities

### New Capabilities
- `docker-infrastructure`: Docker Compose orchestration with Postgres, backend, and frontend services, including Makefile management commands and DB backup/restore.
- `backend-scaffolding`: Spring Boot project scaffold with Clean Architecture layer structure, health endpoint, and Gradle build configuration.
- `frontend-scaffolding`: React + Vite + TypeScript project scaffold with Tailwind CSS, Shadcn UI, and Zustand pre-configured.

### Modified Capabilities
_(none — this is a greenfield project)_

## Impact

- **New files**: `docker-compose.yml`, `Makefile`, `README.md`, `backend/` directory tree, `frontend/` directory tree, Dockerfiles.
- **Dependencies**: Docker & Docker Compose required on the developer's machine. Node.js 18+, JDK 17+, Gradle used inside containers.
- **External tools**: PostgreSQL port 5432 exposed for DBeaver local connections.
- **No breaking changes**: Greenfield project, no existing code affected.
