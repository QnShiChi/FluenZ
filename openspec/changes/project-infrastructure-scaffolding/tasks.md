## 1. Project Root Configuration

- [x] 1.1 Create `.env.example` with all configurable variables (DB credentials, ports) and `.env` with matching defaults
- [x] 1.2 Create `.gitignore` with entries for `.env`, `node_modules/`, `build/`, `.gradle/`, etc.
- [x] 1.3 Create `README.md` with project overview, setup instructions, and DBeaver connection details

## 2. Docker Infrastructure

- [x] 2.1 Create `docker-compose.yml` with three services: `frontend`, `backend`, `postgres` (port 5432 exposed, named volume, `./backups` mount)
- [x] 2.2 Create `Makefile` with targets: `build`, `up`, `down`, `logs`, `clean`, `backup-db`, `restore-db`
- [x] 2.3 Create `backups/` directory with a `.gitkeep` file

## 3. Backend Scaffolding (Spring Boot + Gradle)

- [x] 3.1 Initialize Spring Boot project at `backend/` using Spring Initializr (Gradle Kotlin DSL, Java 17, dependencies: Spring Web, Spring Data JPA, PostgreSQL Driver)
- [x] 3.2 Create Clean Architecture package structure: `controller/`, `service/`, `service/impl/`, `repository/`, `dto/request/`, `dto/response/`, `entity/`, `config/`
- [x] 3.3 Configure `application.yml` with PostgreSQL datasource using environment variables and JPA `ddl-auto: update`
- [x] 3.4 Implement `GET /api/health` endpoint returning `{"status": "UP"}`
- [x] 3.5 Add CORS configuration allowing requests from `http://localhost:5173`
- [x] 3.6 Create `backend/Dockerfile` (multi-stage: Gradle build → JDK 17 slim runtime)

## 4. Frontend Scaffolding (React + Vite + TypeScript)

- [x] 4.1 Initialize Vite React TypeScript project at `frontend/` using `create-vite`
- [x] 4.2 Install and configure Tailwind CSS
- [x] 4.3 Initialize Shadcn UI (new-york style, neutral palette) and configure `components.json`
- [x] 4.4 Install Zustand and create sample store at `src/stores/useAppStore.ts`
- [x] 4.5 Install Axios and create API client at `src/services/api.ts` with base URL `http://localhost:8080/api`
- [x] 4.6 Create directory structure: `src/components/`, `src/hooks/`, `src/services/`, `src/stores/`, `src/pages/`, `src/lib/`
- [x] 4.7 Create `frontend/Dockerfile` (Node 18 Alpine, `npm run dev -- --host 0.0.0.0`)

## 5. Integration Verification

- [x] 5.1 Run `make build` and verify all three Docker images build successfully
- [x] 5.2 Run `make up` and verify all services start and are healthy
- [x] 5.3 Verify `GET http://localhost:8080/api/health` returns `{"status": "UP"}`
- [x] 5.4 Verify frontend is accessible at `http://localhost:5173`
- [x] 5.5 Verify PostgreSQL is connectable from host on `localhost:5432` (DBeaver-ready)
- [x] 5.6 Test `make backup-db` and `make restore-db` round-trip
